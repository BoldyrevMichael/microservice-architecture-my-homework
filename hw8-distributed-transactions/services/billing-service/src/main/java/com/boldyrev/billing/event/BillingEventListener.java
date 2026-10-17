package com.boldyrev.billing.event;

import com.boldyrev.billing.service.AccountNotFoundException;
import com.boldyrev.billing.service.AccountService;
import com.boldyrev.billing.service.InsufficientFundsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class BillingEventListener {

  private final AccountService accountService;
  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final JsonMapper jsonMapper;

  @Autowired
  public BillingEventListener(
      AccountService accountService,
      KafkaTemplate<String, Object> kafkaTemplate,
      JsonMapper jsonMapper) {
    this.accountService = accountService;
    this.kafkaTemplate = kafkaTemplate;
    this.jsonMapper = jsonMapper;
  }

  @KafkaListener(topics = Topics.USER_REGISTERED)
  public void onUserRegistered(String payload) {
    accountService.create(jsonMapper.readValue(payload, UserRegistered.class).userId());
  }

  // шаг саги: списание за заказ и ответное событие с итогом
  @KafkaListener(topics = Topics.ORDER_CREATED)
  public void onOrderCreated(String payload) {
    OrderEvent order = jsonMapper.readValue(payload, OrderEvent.class);
    OrderEvent result;
    try {
      accountService.withdraw(order.userId(), order.price());
      result = order.withStatus("COMPLETED", null);
    } catch (InsufficientFundsException | AccountNotFoundException e) {
      result = order.withStatus("FAILED", e.getMessage());
    }
    kafkaTemplate.send(Topics.ORDER_PAYMENT, String.valueOf(order.orderId()), result);
  }

  // компенсация: если позже отказали склад или доставка, деньги возвращаются
  @KafkaListener(topics = {Topics.ORDER_STOCK, Topics.ORDER_DELIVERY})
  public void onLaterStepFailed(String payload) {
    OrderEvent event = jsonMapper.readValue(payload, OrderEvent.class);
    if (event.failed()) {
      accountService.deposit(event.userId(), event.price());
    }
  }
}

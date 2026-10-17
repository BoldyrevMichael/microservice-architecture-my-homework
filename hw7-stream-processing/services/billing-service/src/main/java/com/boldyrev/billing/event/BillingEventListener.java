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

  // списание за заказ и ответное событие с итогом
  @KafkaListener(topics = Topics.ORDER_CREATED)
  public void onOrderCreated(String payload) {
    OrderCreated order = jsonMapper.readValue(payload, OrderCreated.class);
    OrderPayment result;
    try {
      accountService.withdraw(order.userId(), order.price());
      result = OrderPayment.completed(order);
    } catch (InsufficientFundsException | AccountNotFoundException e) {
      result = OrderPayment.failed(order, e.getMessage());
    }
    kafkaTemplate.send(Topics.ORDER_PAYMENT, String.valueOf(order.orderId()), result);
  }
}

package com.boldyrev.notification.event;

import com.boldyrev.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class NotificationEventListener {

  private final NotificationService notificationService;
  private final JsonMapper jsonMapper;

  @Autowired
  public NotificationEventListener(NotificationService notificationService, JsonMapper jsonMapper) {
    this.notificationService = notificationService;
    this.jsonMapper = jsonMapper;
  }

  @KafkaListener(topics = Topics.USER_REGISTERED)
  public void onUserRegistered(String payload) {
    UserRegistered event = jsonMapper.readValue(payload, UserRegistered.class);
    notificationService.rememberRecipient(event.userId(), event.email());
  }

  @KafkaListener(topics = Topics.ORDER_PAYMENT)
  public void onOrderPayment(String payload) {
    OrderPayment event = jsonMapper.readValue(payload, OrderPayment.class);
    notificationService.orderPaymentResult(
        event.userId(), event.orderId(), event.price(), event.completed(), event.reason());
  }
}

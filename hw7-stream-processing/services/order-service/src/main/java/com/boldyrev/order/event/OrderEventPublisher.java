package com.boldyrev.order.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Autowired
  public OrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void orderCreated(OrderCreated event) {
    kafkaTemplate.send(Topics.ORDER_CREATED, String.valueOf(event.orderId()), event);
  }
}

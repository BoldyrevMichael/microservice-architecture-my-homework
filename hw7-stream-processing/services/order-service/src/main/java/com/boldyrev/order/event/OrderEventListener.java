package com.boldyrev.order.event;

import com.boldyrev.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class OrderEventListener {

  private final OrderService orderService;
  private final JsonMapper jsonMapper;

  @Autowired
  public OrderEventListener(OrderService orderService, JsonMapper jsonMapper) {
    this.orderService = orderService;
    this.jsonMapper = jsonMapper;
  }

  @KafkaListener(topics = Topics.ORDER_PAYMENT)
  public void onOrderPayment(String payload) {
    orderService.applyPayment(jsonMapper.readValue(payload, OrderPayment.class));
  }
}

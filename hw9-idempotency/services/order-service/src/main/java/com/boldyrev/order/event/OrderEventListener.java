package com.boldyrev.order.event;

import com.boldyrev.order.entity.OrderStatus;
import com.boldyrev.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

// итоги шагов саги: оплата, резерв на складе, резерв курьера
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
  public void onPayment(String payload) {
    orderService.applyStep(parse(payload), OrderStatus.PAID);
  }

  @KafkaListener(topics = Topics.ORDER_STOCK)
  public void onStock(String payload) {
    orderService.applyStep(parse(payload), OrderStatus.RESERVED);
  }

  @KafkaListener(topics = Topics.ORDER_DELIVERY)
  public void onDelivery(String payload) {
    orderService.applyStep(parse(payload), OrderStatus.CONFIRMED);
  }

  private OrderEvent parse(String payload) {
    return jsonMapper.readValue(payload, OrderEvent.class);
  }
}

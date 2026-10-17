package com.boldyrev.delivery.event;

import com.boldyrev.delivery.service.CourierService;
import com.boldyrev.delivery.service.SlotTakenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class DeliveryEventListener {

  private final CourierService courierService;
  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final JsonMapper jsonMapper;

  @Autowired
  public DeliveryEventListener(
      CourierService courierService,
      KafkaTemplate<String, Object> kafkaTemplate,
      JsonMapper jsonMapper) {
    this.courierService = courierService;
    this.kafkaTemplate = kafkaTemplate;
    this.jsonMapper = jsonMapper;
  }

  // последний шаг саги: резерв курьера на слот после резерва товара
  @KafkaListener(topics = Topics.ORDER_STOCK)
  public void onStock(String payload) {
    OrderEvent order = jsonMapper.readValue(payload, OrderEvent.class);
    if (order.failed()) {
      return;
    }
    OrderEvent result;
    try {
      courierService.reserve(order.orderId(), order.userId(), order.slot());
      result = order.withStatus("RESERVED", null);
    } catch (SlotTakenException e) {
      result = order.withStatus("FAILED", e.getMessage());
    }
    kafkaTemplate.send(Topics.ORDER_DELIVERY, String.valueOf(order.orderId()), result);
  }
}

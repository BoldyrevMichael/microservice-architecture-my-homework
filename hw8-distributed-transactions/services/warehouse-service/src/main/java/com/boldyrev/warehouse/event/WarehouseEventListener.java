package com.boldyrev.warehouse.event;

import com.boldyrev.warehouse.service.OutOfStockException;
import com.boldyrev.warehouse.service.ProductNotFoundException;
import com.boldyrev.warehouse.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class WarehouseEventListener {

  private final StockService stockService;
  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final JsonMapper jsonMapper;

  @Autowired
  public WarehouseEventListener(
      StockService stockService,
      KafkaTemplate<String, Object> kafkaTemplate,
      JsonMapper jsonMapper) {
    this.stockService = stockService;
    this.kafkaTemplate = kafkaTemplate;
    this.jsonMapper = jsonMapper;
  }

  // шаг саги после оплаты: резерв товара и ответное событие
  @KafkaListener(topics = Topics.ORDER_PAYMENT)
  public void onPayment(String payload) {
    OrderEvent order = jsonMapper.readValue(payload, OrderEvent.class);
    if (order.failed()) {
      return;
    }
    OrderEvent result;
    try {
      stockService.reserve(order.orderId(), order.productId(), order.quantity());
      result = order.withStatus("RESERVED", null);
    } catch (OutOfStockException | ProductNotFoundException e) {
      result = order.withStatus("FAILED", e.getMessage());
    }
    kafkaTemplate.send(Topics.ORDER_STOCK, String.valueOf(order.orderId()), result);
  }

  // компенсация: доставка не нашла курьера — снять резерв
  @KafkaListener(topics = Topics.ORDER_DELIVERY)
  public void onDelivery(String payload) {
    OrderEvent event = jsonMapper.readValue(payload, OrderEvent.class);
    if (event.failed()) {
      stockService.release(event.orderId());
    }
  }
}

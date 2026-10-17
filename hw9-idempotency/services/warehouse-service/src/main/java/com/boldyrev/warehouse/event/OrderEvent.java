package com.boldyrev.warehouse.event;

import java.math.BigDecimal;

// снимок заказа из тем саги; status и reason проставляет шаг-отправитель
public record OrderEvent(
    long orderId,
    long userId,
    BigDecimal price,
    long productId,
    int quantity,
    String slot,
    String status,
    String reason) {

  public boolean failed() {
    return "FAILED".equals(status);
  }

  public OrderEvent withStatus(String newStatus, String newReason) {
    return new OrderEvent(orderId, userId, price, productId, quantity, slot, newStatus, newReason);
  }
}

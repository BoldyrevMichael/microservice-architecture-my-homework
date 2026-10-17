package com.boldyrev.order.event;

import java.math.BigDecimal;

// снимок заказа, который идёт по всем темам саги; status и reason заполняет шаг-отправитель
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
}

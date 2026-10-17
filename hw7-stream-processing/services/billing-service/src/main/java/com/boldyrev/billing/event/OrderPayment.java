package com.boldyrev.billing.event;

import java.math.BigDecimal;

// итог оплаты заказа: COMPLETED — деньги списаны, FAILED — причина в reason
public record OrderPayment(
    long orderId, long userId, BigDecimal price, String status, String reason) {

  public static OrderPayment completed(OrderCreated order) {
    return new OrderPayment(order.orderId(), order.userId(), order.price(), "COMPLETED", null);
  }

  public static OrderPayment failed(OrderCreated order, String reason) {
    return new OrderPayment(order.orderId(), order.userId(), order.price(), "FAILED", reason);
  }
}

package com.boldyrev.order.event;

import java.math.BigDecimal;

public record OrderPayment(
    long orderId, long userId, BigDecimal price, String status, String reason) {

  public boolean completed() {
    return "COMPLETED".equals(status);
  }
}

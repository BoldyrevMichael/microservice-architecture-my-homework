package com.boldyrev.notification.event;

import java.math.BigDecimal;

// итог саги заказа: CONFIRMED или CANCELLED с причиной
public record OrderCompleted(
    long orderId, long userId, BigDecimal price, String status, String reason) {

  public boolean confirmed() {
    return "CONFIRMED".equals(status);
  }
}

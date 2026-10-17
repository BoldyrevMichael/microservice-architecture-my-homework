package com.boldyrev.order.dto;

import com.boldyrev.order.entity.Order;
import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
    long id, long userId, BigDecimal price, String status, String reason, Instant createdAt) {

  public static OrderResponse from(Order order) {
    return new OrderResponse(
        order.getId(),
        order.getUserId(),
        order.getPrice(),
        order.getStatus().name(),
        order.getReason(),
        order.getCreatedAt());
  }
}

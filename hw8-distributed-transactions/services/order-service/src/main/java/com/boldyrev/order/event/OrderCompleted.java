package com.boldyrev.order.event;

import java.math.BigDecimal;

// итог саги для уведомлений: CONFIRMED или CANCELLED с причиной
public record OrderCompleted(
    long orderId, long userId, BigDecimal price, String status, String reason) {}

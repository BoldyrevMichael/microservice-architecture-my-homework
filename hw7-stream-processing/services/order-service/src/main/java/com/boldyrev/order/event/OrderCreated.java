package com.boldyrev.order.event;

import java.math.BigDecimal;

public record OrderCreated(long orderId, long userId, BigDecimal price) {}

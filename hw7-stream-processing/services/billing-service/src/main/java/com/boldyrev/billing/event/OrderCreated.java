package com.boldyrev.billing.event;

import java.math.BigDecimal;

public record OrderCreated(long orderId, long userId, BigDecimal price) {}

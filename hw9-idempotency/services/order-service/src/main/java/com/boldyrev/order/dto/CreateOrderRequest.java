package com.boldyrev.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreateOrderRequest(
    @NotNull @Positive BigDecimal price,
    @NotNull Long productId,
    @NotNull @Positive Integer quantity,
    @NotBlank String slot) {}

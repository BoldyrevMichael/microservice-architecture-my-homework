package com.boldyrev.billing.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record AmountRequest(@NotNull @Positive BigDecimal amount) {}

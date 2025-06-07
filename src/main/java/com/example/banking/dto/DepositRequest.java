package com.example.banking.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * Payload for money deposit requests.
 */
public record DepositRequest(
        @NotNull
        @DecimalMin(value = "1")
        BigDecimal amount) {
}

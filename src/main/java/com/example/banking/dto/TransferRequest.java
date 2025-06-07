package com.example.banking.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Payload for money transfer requests.
 */
public record TransferRequest(
        @NotBlank String toAccount,
        @NotNull @DecimalMin(value = "1") BigDecimal amount,
        @NotBlank String pin) {
}

package com.example.banking.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Payload for account creation requests.
 */
public record AccountRequest(
        @NotBlank String citizenId,
        @NotBlank String thaiName,
        @NotBlank String englishName,
        @PositiveOrZero BigDecimal initialDeposit) {
}

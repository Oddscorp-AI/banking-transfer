package com.example.banking.dto;

/**
 * Payload for account creation requests.
 */
public record AccountRequest(
        String citizenId,
        String thaiName,
        String englishName,
        Double initialDeposit) {
}
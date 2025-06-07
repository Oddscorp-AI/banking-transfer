package com.example.banking.dto;

/**
 * Payload for online registration requests.
 */
public record RegistrationRequest(
        String email,
        String password,
        String citizenId,
        String thaiName,
        String englishName,
        String pin) {
}
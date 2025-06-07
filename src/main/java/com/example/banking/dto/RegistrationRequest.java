package com.example.banking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Payload for online registration requests.
 */
public record RegistrationRequest(
        @Email @NotBlank String email,
        @NotBlank String password,
        @NotBlank String citizenId,
        @NotBlank String thaiName,
        @NotBlank String englishName,
        @Pattern(regexp = "\\d{6}") String pin) {
}
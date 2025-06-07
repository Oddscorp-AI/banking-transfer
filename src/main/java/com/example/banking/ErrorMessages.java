package com.example.banking;

/**
 * Central location for common error message constants.
 */
public final class ErrorMessages {
    public static final String ACCOUNT_NOT_FOUND = "Account not found";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String ACCESS_DENIED = "Access denied";
    public static final String INVALID_PIN = "Invalid PIN";
    public static final String DAILY_LIMIT_EXCEEDED = "Daily transfer limit exceeded";

    private ErrorMessages() {
        // Prevent instantiation
    }
}

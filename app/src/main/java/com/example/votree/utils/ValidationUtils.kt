package com.example.votree.utils

object ValidationUtils {

    /**
     * Validates an email address.
     * @param email The email address to validate.
     * @return true if the email address is valid, false otherwise.
     */
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Validates a phone number to ensure it has exactly 10 digits.
     * @param phoneNumber The phone number to validate.
     * @return true if the phone number is valid, false otherwise.
     */
    fun isValidPhoneNumber(phoneNumber: String): Boolean {
        // Check if the phone number consists of exactly 10 digits.
        return phoneNumber.matches("^\\d{10}$".toRegex())
    }
}
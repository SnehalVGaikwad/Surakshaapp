package com.suraksha.surakshaapp.Utils;

import android.util.Patterns;

public class ValidationUtils {

    public static boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone.length() == 10 && phone.matches("\\d{10}");
    }

    public static boolean isStrongPassword(String password) {
        // Minimum 8 characters, 1 uppercase, 1 lowercase, 1 digit, 1 special char
        if (password.length() < 8) return false;

        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecialChar = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

        return hasUppercase && hasLowercase && hasDigit && hasSpecialChar;
    }
}

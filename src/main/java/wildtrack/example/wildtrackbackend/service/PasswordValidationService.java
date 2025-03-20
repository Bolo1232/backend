package wildtrack.example.wildtrackbackend.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

@Service
public class PasswordValidationService {

    /**
     * Validates that a password meets the security requirements:
     * - At least 8 characters
     * - At least one uppercase letter (A-Z)
     * - At least one lowercase letter (a-z)
     * - At least one digit (0-9)
     * - At least one special character
     *
     * @param password The password to validate
     * @return A validation result containing success status and error message
     */
    public PasswordValidationResult validatePassword(String password) {
        // Check if password is null or empty
        if (password == null || password.isEmpty()) {
            return new PasswordValidationResult(false, "Password cannot be empty");
        }

        // Check minimum length
        if (password.length() < 8) {
            return new PasswordValidationResult(false, "Password must be at least 8 characters long");
        }

        // Check for at least one uppercase letter
        if (!Pattern.compile("[A-Z]").matcher(password).find()) {
            return new PasswordValidationResult(false, "Password must contain at least one uppercase letter");
        }

        // Check for at least one lowercase letter
        if (!Pattern.compile("[a-z]").matcher(password).find()) {
            return new PasswordValidationResult(false, "Password must contain at least one lowercase letter");
        }

        // Check for at least one digit
        if (!Pattern.compile("[0-9]").matcher(password).find()) {
            return new PasswordValidationResult(false, "Password must contain at least one digit");
        }

        // Check for at least one special character
        if (!Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]").matcher(password).find()) {
            return new PasswordValidationResult(false, "Password must contain at least one special character");
        }

        // All validations passed
        return new PasswordValidationResult(true, null);
    }

    /**
     * Class to hold password validation results
     */
    public class PasswordValidationResult {
        private final boolean valid;
        private final String errorMessage;

        public PasswordValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
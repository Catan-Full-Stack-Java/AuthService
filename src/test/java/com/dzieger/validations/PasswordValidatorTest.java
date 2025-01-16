package com.dzieger.validations;

import com.dzieger.annotations.ValidPassword;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PasswordValidatorTest {

    private PasswordValidator passwordValidator;

    @BeforeEach
    void setUp() {
        passwordValidator = new PasswordValidator();
    }

    @Test
    void testPasswordValidator_validPassword() {
        // Valid password
        String validPassword = "Valid@123";
        assertTrue(passwordValidator.isValid(validPassword, null), "Password should be valid");
    }

    @Test
    void testPasswordValidator_invalidPassword_tooShort() {
        // Too short
        String invalidPassword = "Val@1";
        assertFalse(passwordValidator.isValid(invalidPassword, null), "Password is too short and should be invalid");
    }

    @Test
    void testPasswordValidator_invalidPassword_missingUppercase() {
        // Missing uppercase
        String invalidPassword = "valid@123";
        assertFalse(passwordValidator.isValid(invalidPassword, null), "Password missing uppercase letter should be invalid");
    }

    @Test
    void testPasswordValidator_invalidPassword_missingLowercase() {
        // Missing lowercase
        String invalidPassword = "VALID@123";
        assertFalse(passwordValidator.isValid(invalidPassword, null), "Password missing lowercase letter should be invalid");
    }

    @Test
    void testPasswordValidator_invalidPassword_missingDigit() {
        // Missing digit
        String invalidPassword = "Valid@abc";
        assertFalse(passwordValidator.isValid(invalidPassword, null), "Password missing digit should be invalid");
    }

    @Test
    void testPasswordValidator_invalidPassword_missingSpecialCharacter() {
        // Missing special character
        String invalidPassword = "Valid1234";
        assertFalse(passwordValidator.isValid(invalidPassword, null), "Password missing special character should be invalid");
    }

    @Test
    void testPasswordValidator_invalidPassword_containsWhitespace() {
        // Contains whitespace
        String invalidPassword = "Valid 123";
        assertFalse(passwordValidator.isValid(invalidPassword, null), "Password with whitespace should be invalid");
    }

    @Test
    void testPasswordValidator_invalidPassword_exceedsMaxLength() {
        // Exceeds max length (more than 20 characters)
        String invalidPassword = "Valid@1234567890123456789";
        assertFalse(passwordValidator.isValid(invalidPassword, null), "Password exceeding max length should be invalid");
    }
}

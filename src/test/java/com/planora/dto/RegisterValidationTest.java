package com.planora.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RegisterValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenAllFieldsValid_thenNoViolations() {
        RegisterRequestDto dto = new RegisterRequestDto(
                "Alice Sharma", "alice@planora.com", "Secure@12345"
        );

        Set<ConstraintViolation<RegisterRequestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenBlankName_thenViolation() {
        RegisterRequestDto dto = new RegisterRequestDto("", "alice@planora.com", "Secure@12345");

        Set<ConstraintViolation<RegisterRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("fullName")));
    }

    @Test
    void whenInvalidEmail_thenViolation() {
        RegisterRequestDto dto = new RegisterRequestDto("Alice Sharma", "not-an-email", "Secure@12345");

        Set<ConstraintViolation<RegisterRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void whenShortPassword_thenViolation() {
        RegisterRequestDto dto = new RegisterRequestDto("Alice Sharma", "alice@planora.com", "short");

        Set<ConstraintViolation<RegisterRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }
}

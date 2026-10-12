package com.planora.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EventValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenAllFieldsValid_thenNoViolations() {
        EventRequestDto dto = new EventRequestDto(
                "Annual Tech Conference",
                "Conference",
                "Bangalore International Exhibition Centre",
                LocalDate.of(2026, 12, 15),
                500,
                75000.0
        );

        Set<ConstraintViolation<EventRequestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenBlankFieldsAndNegativeBudget_thenViolationsReported() {
        EventRequestDto dto = new EventRequestDto(
                "",         // Blank name
                "",         // Blank type
                "",         // Blank location
                null,       // Null date
                0,          // Invalid guest count (min 1)
                -100.0      // Negative budget
        );

        Set<ConstraintViolation<EventRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("eventName")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("eventType")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("location")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("eventDate")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("guestCount")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("budget")));
    }
}

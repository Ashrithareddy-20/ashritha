package com.planora.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VendorValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenAllFieldsValid_thenNoViolations() {
        VendorRequestDto dto = new VendorRequestDto(
                "Royal Caterers",
                "Catering",
                "Bangalore City",
                "info@royalcaterers.com",
                15000.0,
                4.8
        );

        Set<ConstraintViolation<VendorRequestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenBlankAndInvalidValues_thenViolationsReported() {
        VendorRequestDto dto = new VendorRequestDto(
                "",                 // Blank vendorName
                "",                 // Blank serviceCategory
                "",                 // Blank location
                "not-an-email",     // Invalid email
                -50.0,              // Negative price
                6.5                 // Rating > 5.0
        );

        Set<ConstraintViolation<VendorRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("vendorName")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("serviceCategory")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("location")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("contactEmail")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("price")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("rating")));
    }
}

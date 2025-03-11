package com.acs.bookingsystem.booking.request;

import com.acs.bookingsystem.booking.enums.ClassType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DanceClassRequestTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    void whenValidDanceClassRequest_thenNoViolations() {
        DanceClassRequest request = new DanceClassRequest (
                ClassType.PRIVATE,
                new BigDecimal("25.50")
        );

        Set<ConstraintViolation<DanceClassRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenNullClassType_thenValidationFails() {
        DanceClassRequest request = new DanceClassRequest(
                null,
                new BigDecimal("25.50")
        );

        Set<ConstraintViolation<DanceClassRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void whenNullPricePerHour_thenValidationFails() {
        DanceClassRequest request = new DanceClassRequest(
                ClassType.PRIVATE,
                null
        );

        Set<ConstraintViolation<DanceClassRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void whenNegativePrice_thenValidationFails() {
        DanceClassRequest request = new DanceClassRequest(
                ClassType.PRIVATE,
                new BigDecimal("-10.00")
        );

        Set<ConstraintViolation<DanceClassRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void whenTooManyDecimals_thenValidationFails() {
        DanceClassRequest request = new DanceClassRequest(
                ClassType.PRIVATE,
                new BigDecimal("25.555")
        );

        Set<ConstraintViolation<DanceClassRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void whenPriceExceedsThreeDigits_thenValidationFails() {
        DanceClassRequest request = new DanceClassRequest(
                ClassType.PRIVATE,
                new BigDecimal("1000.00")
        );

        Set<ConstraintViolation<DanceClassRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

}
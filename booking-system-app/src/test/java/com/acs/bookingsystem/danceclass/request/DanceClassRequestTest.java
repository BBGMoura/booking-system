package com.acs.bookingsystem.danceclass.request;

import com.acs.bookingsystem.danceclass.enums.ClassType;
import com.acs.bookingsystem.user.enums.Role;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class DanceClassRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @ParameterizedTest(name = "Dance class validation: {4}")
    @MethodSource("provideDanceClassRequestArguments")
    void validateDanceClassRequest(ClassType classType, BigDecimal price, Role role, boolean expectedToBeValid, String description) {
        DanceClassRequest request = new DanceClassRequest(classType, price, role);

        Set<ConstraintViolation<DanceClassRequest>> violations = validator.validate(request);

        assertEquals(expectedToBeValid, violations.isEmpty(), description);
    }

    private static Stream<Arguments> provideDanceClassRequestArguments() {
        return Stream.of(
                // Valid case
                Arguments.of(ClassType.PRIVATE, new BigDecimal("25.50"), Role.ROLE_USER, true,
                             "should pass with valid ClassType, price, and Role"),

                // Invalid cases
                Arguments.of(null, new BigDecimal("25.50"), Role.ROLE_USER, false,
                             "should fail when ClassType is null"),
                Arguments.of(ClassType.PRIVATE, null, Role.ROLE_USER, false,
                             "should fail when price is null"),
                Arguments.of(ClassType.PRIVATE, new BigDecimal("-10.00"), Role.ROLE_USER, false,
                             "should fail when price is negative"),
                Arguments.of(ClassType.PRIVATE, new BigDecimal("25.555"), Role.ROLE_USER, false,
                             "should fail when price has invalid decimal scale"),
                Arguments.of(ClassType.PRIVATE, new BigDecimal("1000.00"), Role.ROLE_USER, false,
                             "should fail when price exceeds maximum value"),
                Arguments.of(ClassType.PRIVATE, new BigDecimal("25.50"), null, false,
                             "should fail when Role is null")
        );
    }
}
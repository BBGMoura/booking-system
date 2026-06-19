package com.acs.bookingsystem.user.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UpdateUserRequestTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    void validUpdateRequest() {
        UpdateUserRequest request = new UpdateUserRequest(
                "john.doe@example.com",
                "Password1!"
        );

        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void nullValuesValid() {
        // All fields can be null in UpdateUserRequest
        UpdateUserRequest request = new UpdateUserRequest(
                null,
                null
        );

        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("invalidEmailProvider")
    void emailInvalid(String email) {
        UpdateUserRequest request = new UpdateUserRequest(
                email,
                "Password1!"
        );

        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    static Stream<String> invalidEmailProvider() {
        return Stream.of(
                "invalid",
                "invalid@",
                "invalid@domain",
                "invalid@domain.",
                "@domain.com",
                "invalid@.com",
                "a".repeat(245) + "@domain.com"
        );
    }

    @ParameterizedTest
    @MethodSource("validEmailProvider")
    void emailValid(String email) {
        UpdateUserRequest request = new UpdateUserRequest(
                email,
                "Password1!"
        );

        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty() ||
                violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    static Stream<String> validEmailProvider() {
        return Stream.of(
                "simple@example.com",
                "very.common@example.com",
                "disposable.style.email.with+symbol@example.com",
                "other.email-with-hyphen@example.com",
                "fully-qualified-domain@example.com",
                "user.name+tag+sorting@example.com",
                "x@example.com",
                "example-indeed@strange-example.com",
                "example@s.example"
        );
    }

    @ParameterizedTest
    @MethodSource("invalidPasswordProvider")
    void passwordInvalid(String password) {
        UpdateUserRequest request = new UpdateUserRequest(
                "john.doe@example.com",
                password
        );

        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    static Stream<String> invalidPasswordProvider() {
        return Stream.of(
                "password",  // No uppercase, digit, or special char
                "PASSWORD1", // No lowercase or special char
                "Password1", // No special char
                "Password!", // No digit
                "Passw1!",   // Too short (less than 8)
                "Password1!Password1!Password1!", // Too long (more than 16)
                "password1!" // No uppercase
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"Password1!", "Abcdef1!", "Pa55w0rd!", "Secret123$", "Test1234*"})
    void passwordValid(String password) {
        UpdateUserRequest request = new UpdateUserRequest(
                "john.doe@example.com",
                password
        );

        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty() ||
                violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

}
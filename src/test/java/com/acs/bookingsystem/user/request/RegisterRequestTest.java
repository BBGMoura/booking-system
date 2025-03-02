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

class RegisterRequestTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    void validRegisterRequest() {
        RegisterRequest request = new RegisterRequest(
                "John",
                "Doe",
                "john.doe@example.com",
                "07112233445",
                "Password1!"
        );

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void firstNameBlank(String firstName) {
        RegisterRequest request = new RegisterRequest(
                firstName,
                "Doe",
                "john.doe@example.com",
                "07112233445",
                "Password1!"
        );

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("firstName")));
    }

    @Test
    void firstNameTooLong() {
        RegisterRequest request = new RegisterRequest(
                "J".repeat(51),
                "Doe",
                "john.doe@example.com",
                "07112233445",
                "Password1!"
        );

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("firstName")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void lastNameBlank(String lastName) {
        RegisterRequest request = new RegisterRequest(
                "John",
                lastName,
                "john.doe@example.com",
                "07112233445",
                "Password1!"
        );

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("lastName")));
    }

    @Test
    void lastNameTooLong() {
        RegisterRequest request = new RegisterRequest(
                "John",
                "D".repeat(51),
                "john.doe@example.com",
                "07112233445",
                "Password1!"
        );

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("lastName")));
    }

    @ParameterizedTest
    @MethodSource("invalidEmailProvider")
    void emailInvalid(String email) {
        RegisterRequest request = new RegisterRequest(
                "John",
                "Doe",
                email,
                "07112233445",
                "Password1!"
        );

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    static Stream<String> invalidEmailProvider() {
        return Stream.of(
                "",
                " ",
                "invalid",
                "invalid@",
                "invalid@domain",
                "invalid@domain.",
                "@domain.com",
                "invalid@.com",
                "a".repeat(245) + "@domain.com" // exceeds max length
        );
    }

    @ParameterizedTest
    @MethodSource("validEmailProvider")
    void emailValid(String email) {
        RegisterRequest request = new RegisterRequest(
                "John",
                "Doe",
                email,
                "07112233445",
                "Password1!"
        );

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
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
    @MethodSource("invalidPhoneProvider")
    void phoneNumberInvalid(String phoneNumber) {
        RegisterRequest request = new RegisterRequest(
                "John",
                "Doe",
                "john.doe@example.com",
                phoneNumber,
                "Password1!"
        );

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("phoneNumber")));
    }

    static Stream<String> invalidPhoneProvider() {
        return Stream.of(
                "",
                " ",
                "123456789",  // Doesn't start with 0
                "1711223344", // Doesn't start with 0
                "071122334",  // Too short
                "071122334455", // Too long
                "+447112233445" // Wrong format (no + allowed)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"07112233445", "0711223344"})
    void phoneNumberValid(String phoneNumber) {
        RegisterRequest request = new RegisterRequest(
                "John",
                "Doe",
                "john.doe@example.com",
                phoneNumber,
                "Password1!"
        );

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty() ||
                violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("phoneNumber")));
    }

    @ParameterizedTest
    @MethodSource("invalidPasswordProvider")
    void passwordInvalid(String password) {
        RegisterRequest request = new RegisterRequest(
                "John",
                "Doe",
                "john.doe@example.com",
                "07112233445",
                password
        );

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    static Stream<String> invalidPasswordProvider() {
        return Stream.of(
                "",
                " ",
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
        RegisterRequest request = new RegisterRequest(
                "John",
                "Doe",
                "john.doe@example.com",
                "07112233445",
                password
        );

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty() ||
                violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("password")));
    }
}
package com.acs.bookingsystem.user.request;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class UpdatePasswordRequestTest {

  private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
  private final Validator validator = factory.getValidator();

  @Test
  void validUpdateRequest() {
    UpdatePasswordRequest request = new UpdatePasswordRequest("Password1!");

    Set<ConstraintViolation<UpdatePasswordRequest>> violations = validator.validate(request);
    assertTrue(violations.isEmpty());
  }

  @Test
  void nullPasswordValid() {
    // password can be null (no-op update)
    UpdatePasswordRequest request = new UpdatePasswordRequest(null);

    Set<ConstraintViolation<UpdatePasswordRequest>> violations = validator.validate(request);
    assertTrue(violations.isEmpty());
  }

  @ParameterizedTest
  @MethodSource("invalidPasswordProvider")
  void passwordInvalid(String password) {
    UpdatePasswordRequest request = new UpdatePasswordRequest(password);

    Set<ConstraintViolation<UpdatePasswordRequest>> violations = validator.validate(request);
    assertFalse(violations.isEmpty());
    assertTrue(
        violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
  }

  static Stream<String> invalidPasswordProvider() {
    return Stream.of(
        "password", // No uppercase, digit, or special char
        "PASSWORD1", // No lowercase or special char
        "Password1", // No special char
        "Password!", // No digit
        "Passw1!", // Too short (less than 8)
        "Password1!Password1!Password1!", // Too long (more than 16)
        "password1!" // No uppercase
        );
  }

  @ParameterizedTest
  @ValueSource(strings = {"Password1!", "Abcdef1!", "Pa55w0rd!", "Secret123$", "Test1234*"})
  void passwordValid(String password) {
    UpdatePasswordRequest request = new UpdatePasswordRequest(password);

    Set<ConstraintViolation<UpdatePasswordRequest>> violations = validator.validate(request);
    assertTrue(violations.isEmpty());
  }
}

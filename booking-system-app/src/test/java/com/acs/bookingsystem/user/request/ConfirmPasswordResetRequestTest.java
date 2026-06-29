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

class ConfirmPasswordResetRequestTest {

  private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
  private final Validator validator = factory.getValidator();

  @Test
  void validRequest() {
    ConfirmPasswordResetRequest request =
        new ConfirmPasswordResetRequest("some-jwt-token", "Password1!");

    Set<ConstraintViolation<ConfirmPasswordResetRequest>> violations = validator.validate(request);
    assertTrue(violations.isEmpty());
  }

  @ParameterizedTest
  @ValueSource(strings = {"", " "})
  void tokenBlank(String token) {
    ConfirmPasswordResetRequest request = new ConfirmPasswordResetRequest(token, "Password1!");

    Set<ConstraintViolation<ConfirmPasswordResetRequest>> violations = validator.validate(request);
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("token")));
  }

  @ParameterizedTest
  @MethodSource("invalidPasswordProvider")
  void passwordInvalid(String password) {
    ConfirmPasswordResetRequest request =
        new ConfirmPasswordResetRequest("some-jwt-token", password);

    Set<ConstraintViolation<ConfirmPasswordResetRequest>> violations = validator.validate(request);
    assertFalse(violations.isEmpty());
    assertTrue(
        violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("newPassword")));
  }

  static Stream<String> invalidPasswordProvider() {
    return Stream.of(
        "",
        " ",
        "password",
        "PASSWORD1",
        "Password1",
        "Password!",
        "Passw1!",
        "Password1!Password1!Password1!",
        "password1!");
  }

  @ParameterizedTest
  @ValueSource(strings = {"Password1!", "Abcdef1!", "Pa55w0rd!", "Secret123$", "Test1234*"})
  void passwordValid(String password) {
    ConfirmPasswordResetRequest request =
        new ConfirmPasswordResetRequest("some-jwt-token", password);

    Set<ConstraintViolation<ConfirmPasswordResetRequest>> violations = validator.validate(request);
    assertTrue(
        violations.isEmpty()
            || violations.stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("newPassword")));
  }
}

package com.acs.bookingsystem.common.exception.handler;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acs.bookingsystem.common.exception.AuthorizationException;
import com.acs.bookingsystem.common.exception.LockTimeoutException;
import com.acs.bookingsystem.common.exception.NotFoundException;
import com.acs.bookingsystem.common.exception.RequestException;
import com.acs.bookingsystem.common.exception.model.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

class UniversalExceptionHandlerTest {

  private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setup() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new TestController())
            .setControllerAdvice(new UniversalExceptionHandler())
            .build();
  }

  @RestController
  static class TestController {

    @GetMapping("/test/request-exception")
    void throwRequestException() {
      throw new RequestException("User not found", ErrorCode.INVALID_USER_UID);
    }

    @GetMapping("/test/not-found")
    void throwNotFoundException() {
      throw new NotFoundException("Booking not found", ErrorCode.INVALID_BOOKING_ID);
    }

    @PostMapping("/test/validation")
    void throwFieldValidation(@RequestBody @Valid ValidationRequest body) {
      // intentionally empty — exists only to trigger @Valid on the request body
    }

    @GetMapping("/test/constraint")
    void throwConstraintViolation() {
      try (var factory = Validation.buildDefaultValidatorFactory()) {
        Set<ConstraintViolation<ValidationRequest>> violations =
            factory.getValidator().validate(new ValidationRequest("", null));
        throw new ConstraintViolationException(violations);
      }
    }

    @GetMapping("/test/authorization")
    void throwAuthorizationException() {
      throw new AuthorizationException(
          "Access denied for this resource", ErrorCode.AUTHENTICATION_ERROR);
    }

    @GetMapping("/test/optimistic-lock")
    void throwOptimisticLockingFailure() {
      throw new ObjectOptimisticLockingFailureException(Object.class, 1L);
    }

    @GetMapping("/test/lock-timeout")
    void throwLockTimeoutException() {
      throw new LockTimeoutException(
          ErrorCode.BOOKING_LOCK_TIMEOUT.getDescription(),
          new RuntimeException(),
          ErrorCode.BOOKING_LOCK_TIMEOUT);
    }

    @GetMapping("/test/bad-credentials")
    void throwBadCredentials() {
      throw new BadCredentialsException("Bad credentials");
    }

    @GetMapping("/test/locked")
    void throwLocked() {
      throw new LockedException("Account locked");
    }

    @GetMapping("/test/disabled")
    void throwDisabled() {
      throw new DisabledException("Account disabled");
    }

    @GetMapping("/test/missing-param")
    void requireParam(@RequestParam boolean enable) {}
  }

  record ValidationRequest(@NotBlank String email, @NotBlank String name) {}

  @Test
  void givenRequestException_shouldReturn400WithErrorCodeAndMessage() throws Exception {
    mockMvc
        .perform(get("/test/request-exception"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("INVALID_USER_UID"))
        .andExpect(jsonPath("$.message").value("User not found"))
        .andExpect(jsonPath("$.details").isArray())
        .andExpect(jsonPath("$.details").isEmpty());
  }

  @Test
  void givenNotFoundException_shouldReturn404WithErrorCodeAndMessage() throws Exception {
    mockMvc
        .perform(get("/test/not-found"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("INVALID_BOOKING_ID"))
        .andExpect(jsonPath("$.message").value("Booking not found"))
        .andExpect(jsonPath("$.details").isEmpty());
  }

  @Test
  void givenInvalidRequestBody_shouldReturn400WithValidationDetails() throws Exception {
    String body = objectMapper.writeValueAsString(new ValidationRequest("", ""));

    mockMvc
        .perform(post("/test/validation").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.details", hasSize(2)))
        .andExpect(jsonPath("$.details[*].field", containsInAnyOrder("email", "name")));
  }

  @Test
  void givenConstraintViolation_shouldReturn400WithValidationDetails() throws Exception {
    mockMvc
        .perform(get("/test/constraint"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.details", hasSize(2)))
        .andExpect(jsonPath("$.details[*].field", containsInAnyOrder("email", "name")));
  }

  @Test
  void givenMalformedJson_shouldReturn400WithInvalidFormat() throws Exception {
    mockMvc
        .perform(
            post("/test/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("not valid json {"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("INVALID_FORMAT"))
        .andExpect(jsonPath("$.details").isEmpty());
  }

  @Test
  void givenLockTimeoutException_shouldReturn503WithErrorCodeAndMessage() throws Exception {
    mockMvc
        .perform(get("/test/lock-timeout"))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.status").value(503))
        .andExpect(jsonPath("$.error").value("BOOKING_LOCK_TIMEOUT"))
        .andExpect(jsonPath("$.message").value(ErrorCode.BOOKING_LOCK_TIMEOUT.getDescription()))
        .andExpect(jsonPath("$.details").isEmpty());
  }

  @Test
  void givenOptimisticLockingFailure_shouldReturn409WithConflictError() throws Exception {
    mockMvc
        .perform(get("/test/optimistic-lock"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.error").value("CONFLICT"))
        .andExpect(jsonPath("$.details").isEmpty());
  }

  @Test
  void givenAuthorizationException_shouldReturn403WithMessage() throws Exception {
    mockMvc
        .perform(get("/test/authorization"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.error").value("AUTHENTICATION_ERROR"))
        .andExpect(jsonPath("$.message").value("Access denied for this resource"))
        .andExpect(jsonPath("$.details").isEmpty());
  }

  @Test
  void givenBadCredentials_shouldReturn403WithFriendlyMessage() throws Exception {
    mockMvc
        .perform(get("/test/bad-credentials"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value("AUTHENTICATION_ERROR"))
        .andExpect(jsonPath("$.message").value("Invalid email or password."))
        .andExpect(jsonPath("$.details").isEmpty());
  }

  @Test
  void givenLockedAccount_shouldReturn403WithFriendlyMessage() throws Exception {
    mockMvc
        .perform(get("/test/locked"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value("Account is locked. Please contact support."));
  }

  @Test
  void givenDisabledAccount_shouldReturn403WithFriendlyMessage() throws Exception {
    mockMvc
        .perform(get("/test/disabled"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value("Account is disabled. Please contact support."));
  }

  @Test
  void givenMissingRequiredRequestParam_shouldReturn400WithParamName() throws Exception {
    mockMvc
        .perform(get("/test/missing-param"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.details[0].field").value("enable"))
        .andExpect(jsonPath("$.details[0].message").value("Required parameter is missing"));
  }
}

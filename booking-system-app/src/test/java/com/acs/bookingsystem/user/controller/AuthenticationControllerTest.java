package com.acs.bookingsystem.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acs.bookingsystem.common.ratelimit.ClientIpResolver;
import com.acs.bookingsystem.common.ratelimit.RateLimitAspect;
import com.acs.bookingsystem.common.ratelimit.RateLimitProperties;
import com.acs.bookingsystem.common.ratelimit.RateLimiter;
import com.acs.bookingsystem.security.config.SecurityConfig;
import com.acs.bookingsystem.security.util.JwtUtil;
import com.acs.bookingsystem.user.request.AuthenticateRequest;
import com.acs.bookingsystem.user.request.ConfirmPasswordResetRequest;
import com.acs.bookingsystem.user.request.RegisterRequest;
import com.acs.bookingsystem.user.request.ResetPasswordRequest;
import com.acs.bookingsystem.user.response.AuthenticateResponse;
import com.acs.bookingsystem.user.response.RegisterResponse;
import com.acs.bookingsystem.user.service.AuthenticationService;
import com.acs.bookingsystem.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.caffeine.CaffeineProxyManager;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(AuthenticationController.class)
@Import({SecurityConfig.class, AuthenticationControllerTest.RateLimitTestConfig.class})
@TestPropertySource(
    properties = {
      "rate-limit.buckets.login.capacity=5",
      "rate-limit.buckets.login.refill-period=10m",
      "rate-limit.buckets.passwordResetIp.capacity=5",
      "rate-limit.buckets.passwordResetIp.refill-period=10m",
      "rate-limit.buckets.checkInvite.capacity=5",
      "rate-limit.buckets.checkInvite.refill-period=10m"
    })
class AuthenticationControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private AuthenticationService authenticationService;
  @MockitoBean private UserService userService;
  @MockitoBean private JwtUtil jwtUtil;
  @MockitoBean private AuthenticationProvider authenticationProvider;

  @Test
  void givenValidRequest_whenRegister_thenReturns201WithToken() throws Exception {
    RegisterRequest request =
        new RegisterRequest("Test", "User", "test@example.com", "07123456789", "Password1!");
    RegisterResponse response =
        RegisterResponse.builder()
            .token("jwt-token")
            .uid(UUID.randomUUID())
            .email("test@example.com")
            .firstName("Test")
            .lastName("User")
            .phoneNumber("07123456789")
            .build();
    when(authenticationService.register(any())).thenReturn(response);

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.token").value("jwt-token"))
        .andExpect(jsonPath("$.email").value("test@example.com"));
  }

  @Test
  void givenInvalidBody_whenRegister_thenReturns400() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void givenValidCredentials_whenLogin_thenReturns200WithToken() throws Exception {
    AuthenticateRequest request = new AuthenticateRequest("test@example.com", "Password1!");
    AuthenticateResponse response = AuthenticateResponse.builder().token("jwt-token").build();
    when(authenticationService.authenticate(any())).thenReturn(response);

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("jwt-token"));
  }

  @Test
  void givenInvitedEmail_whenCheckInvite_thenReturnsTrue() throws Exception {
    when(userService.isEmailInvited("invited@example.com")).thenReturn(true);

    mockMvc
        .perform(get("/api/v1/auth/invitations").param("email", "invited@example.com"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.invited").value(true))
        .andExpect(jsonPath("$.email").value("invited@example.com"));
  }

  @Test
  void givenNotInvitedEmail_whenCheckInvite_thenReturnsFalse() throws Exception {
    when(userService.isEmailInvited("unknown@example.com")).thenReturn(false);

    mockMvc
        .perform(get("/api/v1/auth/invitations").param("email", "unknown@example.com"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.invited").value(false));
  }

  @Test
  void givenSixthRequestFromSameIp_whenCheckInvite_thenReturns429() throws Exception {
    when(userService.isEmailInvited("invited@example.com")).thenReturn(true);

    RequestPostProcessor fromDedicatedTestIp =
        req -> {
          req.setRemoteAddr("10.10.10.52");
          return req;
        };

    for (int i = 0; i < 5; i++) {
      mockMvc
          .perform(
              get("/api/v1/auth/invitations")
                  .with(fromDedicatedTestIp)
                  .param("email", "invited@example.com"))
          .andExpect(status().isOk());
    }

    mockMvc
        .perform(
            get("/api/v1/auth/invitations")
                .with(fromDedicatedTestIp)
                .param("email", "invited@example.com"))
        .andExpect(status().isTooManyRequests())
        .andExpect(header().exists("Retry-After"));
  }

  @Test
  void givenValidEmail_whenResetPassword_thenReturns200() throws Exception {
    ResetPasswordRequest request = new ResetPasswordRequest("test@example.com");

    mockMvc
        .perform(
            post("/api/v1/auth/password-reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    verify(authenticationService).resetPassword("test@example.com");
  }

  @Test
  void givenValidTokenAndPassword_whenConfirmReset_thenReturns200() throws Exception {
    ConfirmPasswordResetRequest request =
        new ConfirmPasswordResetRequest("some-jwt-token", "NewPass1!");

    mockMvc
        .perform(
            put("/api/v1/auth/password-reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    verify(authenticationService).confirmPasswordReset("some-jwt-token", "NewPass1!");
  }

  @Test
  void givenBlankToken_whenConfirmReset_thenReturns400() throws Exception {
    mockMvc
        .perform(
            put("/api/v1/auth/password-reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"\",\"newPassword\":\"NewPass1!\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void givenWeakPassword_whenConfirmReset_thenReturns400() throws Exception {
    mockMvc
        .perform(
            put("/api/v1/auth/password-reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"tok\",\"newPassword\":\"weak\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void givenPublicEndpoints_whenNoAuth_thenReturns2xx() throws Exception {
    when(authenticationService.authenticate(any()))
        .thenReturn(AuthenticateResponse.builder().token("t").build());

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(new AuthenticateRequest("a@b.com", "pass"))))
        .andExpect(status().isOk());
  }

  @Test
  void givenSixthRequestFromSameIp_whenLogin_thenReturns429() throws Exception {
    AuthenticateRequest request = new AuthenticateRequest("limited@example.com", "Password1!");
    when(authenticationService.authenticate(any()))
        .thenReturn(AuthenticateResponse.builder().token("jwt-token").build());

    RequestPostProcessor fromDedicatedTestIp =
        req -> {
          req.setRemoteAddr("10.10.10.99");
          return req;
        };

    for (int i = 0; i < 5; i++) {
      mockMvc
          .perform(
              post("/api/v1/auth/login")
                  .with(fromDedicatedTestIp)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk());
    }

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .with(fromDedicatedTestIp)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isTooManyRequests())
        .andExpect(header().exists("Retry-After"));
  }

  @Test
  void givenFourResetsForSameEmail_whenResetPassword_thenReturns429() throws Exception {
    ResetPasswordRequest request = new ResetPasswordRequest("email-limit-test@example.com");

    RequestPostProcessor fromDedicatedTestIp =
        req -> {
          req.setRemoteAddr("10.10.10.50");
          return req;
        };

    for (int i = 0; i < 3; i++) {
      mockMvc
          .perform(
              post("/api/v1/auth/password-reset")
                  .with(fromDedicatedTestIp)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk());
    }

    mockMvc
        .perform(
            post("/api/v1/auth/password-reset")
                .with(fromDedicatedTestIp)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isTooManyRequests())
        .andExpect(header().exists("Retry-After"));
  }

  @Test
  void givenSixResetsFromSameIpForDifferentEmails_whenResetPassword_thenReturns429()
      throws Exception {
    RequestPostProcessor fromDedicatedTestIp =
        req -> {
          req.setRemoteAddr("10.10.10.51");
          return req;
        };

    for (int i = 0; i < 5; i++) {
      ResetPasswordRequest request =
          new ResetPasswordRequest("ip-limit-test-" + i + "@example.com");
      mockMvc
          .perform(
              post("/api/v1/auth/password-reset")
                  .with(fromDedicatedTestIp)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk());
    }

    ResetPasswordRequest request = new ResetPasswordRequest("ip-limit-test-5@example.com");
    mockMvc
        .perform(
            post("/api/v1/auth/password-reset")
                .with(fromDedicatedTestIp)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isTooManyRequests())
        .andExpect(header().exists("Retry-After"));
  }

  @TestConfiguration
  @EnableAspectJAutoProxy(proxyTargetClass = true)
  @EnableConfigurationProperties(RateLimitProperties.class)
  static class RateLimitTestConfig {

    @Bean
    ProxyManager<String> rateLimitProxyManager() {
      return new CaffeineProxyManager<>(
          Caffeine.newBuilder().maximumSize(100), Duration.ofHours(1));
    }

    @Bean
    ClientIpResolver clientIpResolver() {
      return new ClientIpResolver();
    }

    @Bean
    RateLimiter rateLimiter(RateLimitProperties properties, ProxyManager<String> proxyManager) {
      return new RateLimiter(properties, proxyManager);
    }

    @Bean
    RateLimitAspect rateLimitAspect(RateLimiter rateLimiter, ClientIpResolver clientIpResolver) {
      return new RateLimitAspect(rateLimiter, clientIpResolver);
    }
  }
}

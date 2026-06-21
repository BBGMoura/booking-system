package com.acs.bookingsystem.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acs.bookingsystem.security.config.SecurityConfig;
import com.acs.bookingsystem.security.util.JwtUtil;
import com.acs.bookingsystem.user.request.AuthenticateRequest;
import com.acs.bookingsystem.user.request.RegisterRequest;
import com.acs.bookingsystem.user.request.ResetPasswordRequest;
import com.acs.bookingsystem.user.response.AuthenticateResponse;
import com.acs.bookingsystem.user.response.RegisterResponse;
import com.acs.bookingsystem.user.service.AuthenticationService;
import com.acs.bookingsystem.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthenticationController.class)
@Import(SecurityConfig.class)
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
  void givenValidEmail_whenResetPassword_thenReturns204() throws Exception {
    ResetPasswordRequest request = new ResetPasswordRequest("test@example.com");

    mockMvc
        .perform(
            post("/api/v1/auth/password-reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());

    verify(authenticationService).resetPassword("test@example.com");
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
}

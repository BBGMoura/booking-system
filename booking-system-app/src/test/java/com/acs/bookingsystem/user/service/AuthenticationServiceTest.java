package com.acs.bookingsystem.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.acs.bookingsystem.common.email.EmailService;
import com.acs.bookingsystem.security.util.JwtUtil;
import com.acs.bookingsystem.security.util.PasswordUtil;
import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.enums.Role;
import com.acs.bookingsystem.user.request.AuthenticateRequest;
import com.acs.bookingsystem.user.request.RegisterRequest;
import com.acs.bookingsystem.user.request.UpdateUserRequest;
import com.acs.bookingsystem.user.response.AuthenticateResponse;
import com.acs.bookingsystem.user.response.RegisterResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

  @Mock private AuthenticationManager authenticationManager;
  @Mock private UserService userService;
  @Mock private JwtUtil jwtUtil;
  @Mock private PasswordUtil passwordUtil;
  @Mock private EmailService emailService;

  @InjectMocks private AuthenticationService authenticationService;

  private final User user =
      User.builder()
          .uid(UUID.randomUUID())
          .email("test@example.com")
          .role(Role.ROLE_USER)
          .firstName("Test")
          .lastName("User")
          .phoneNumber("07123456789")
          .enabled(true)
          .locked(false)
          .build();

  // --- authenticate ---

  @Test
  void givenValidCredentials_whenAuthenticate_thenReturnsToken() {
    AuthenticateRequest request = new AuthenticateRequest("test@example.com", "Password1!");
    UserDetails principal = mock(UserDetails.class);
    Authentication auth = mock(Authentication.class);
    when(auth.getPrincipal()).thenReturn(principal);
    when(authenticationManager.authenticate(any())).thenReturn(auth);
    when(jwtUtil.generateToken(principal)).thenReturn("jwt-token");

    AuthenticateResponse response = authenticationService.authenticate(request);

    assertThat(response.token()).isEqualTo("jwt-token");
    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
  }

  // --- register ---

  @Test
  void givenValidRequest_whenRegister_thenEncodesPasswordAndReturnsFullResponse() {
    RegisterRequest request =
        new RegisterRequest("Test", "User", "test@example.com", "07123456789", "Password1!");
    when(passwordUtil.encodePassword("Password1!")).thenReturn("encoded");
    when(userService.registerUser(request, "encoded")).thenReturn(user);
    when(jwtUtil.generateToken(user)).thenReturn("jwt-token");

    RegisterResponse response = authenticationService.register(request);

    assertThat(response.token()).isEqualTo("jwt-token");
    assertThat(response.email()).isEqualTo("test@example.com");
    assertThat(response.uid()).isEqualTo(user.getUid());
    verify(passwordUtil).encodePassword("Password1!");
  }

  // --- updateUserCredentials ---

  @Test
  void givenPasswordProvided_whenUpdateCredentials_thenEncodesAndUpdates() {
    UpdateUserRequest request = new UpdateUserRequest("new@example.com", "NewPass1!");
    when(passwordUtil.encodePassword("NewPass1!")).thenReturn("encodedNew");
    when(userService.updateUserCredentials(user, "new@example.com", "encodedNew")).thenReturn(user);
    when(jwtUtil.generateToken(user)).thenReturn("new-token");

    AuthenticateResponse response = authenticationService.updateUserCredentials(user, request);

    assertThat(response.token()).isEqualTo("new-token");
    verify(passwordUtil).encodePassword("NewPass1!");
  }

  @Test
  void givenNoPassword_whenUpdateCredentials_thenSkipsEncoding() {
    UpdateUserRequest request = new UpdateUserRequest("new@example.com", null);
    when(userService.updateUserCredentials(user, "new@example.com", null)).thenReturn(user);
    when(jwtUtil.generateToken(user)).thenReturn("token");

    authenticationService.updateUserCredentials(user, request);

    verify(passwordUtil, never()).encodePassword(any());
  }

  // --- resetPassword ---

  @Test
  void givenValidEmail_whenResetPassword_thenGeneratesEncodesAndSendsEmail() {
    when(passwordUtil.generateNewPassword()).thenReturn("NewRandom1!");
    when(passwordUtil.encodePassword("NewRandom1!")).thenReturn("encodedRandom");

    authenticationService.resetPassword("test@example.com");

    verify(userService).resetPassword("test@example.com", "encodedRandom");
    verify(emailService).sendPasswordResetEmail("test@example.com", "NewRandom1!");
  }
}

package com.acs.bookingsystem.user.controller;

import com.acs.bookingsystem.common.ratelimit.RateLimit;
import com.acs.bookingsystem.common.ratelimit.RateLimitKeyType;
import com.acs.bookingsystem.user.request.AuthenticateRequest;
import com.acs.bookingsystem.user.request.ConfirmPasswordResetRequest;
import com.acs.bookingsystem.user.request.RegisterRequest;
import com.acs.bookingsystem.user.request.ResetPasswordRequest;
import com.acs.bookingsystem.user.response.AuthenticateResponse;
import com.acs.bookingsystem.user.response.CheckInviteResponse;
import com.acs.bookingsystem.user.response.RegisterResponse;
import com.acs.bookingsystem.user.service.AuthenticationService;
import com.acs.bookingsystem.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthenticationController {

  private final AuthenticationService authenticationService;
  private final UserService userService;

  @PostMapping("/register")
  @RateLimit(bucket = "register", key = RateLimitKeyType.IP)
  public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(authenticationService.register(request));
  }

  @PostMapping("/login")
  @RateLimit(bucket = "login", key = RateLimitKeyType.IP)
  public ResponseEntity<AuthenticateResponse> authenticate(
      @Valid @RequestBody AuthenticateRequest request) {
    return ResponseEntity.ok(authenticationService.authenticate(request));
  }

  @GetMapping("/invitations")
  @RateLimit(bucket = "checkInvite", key = RateLimitKeyType.IP)
  public ResponseEntity<CheckInviteResponse> checkInvite(@RequestParam @Email String email) {
    return ResponseEntity.ok(new CheckInviteResponse(email, userService.isEmailInvited(email)));
  }

  @PostMapping("/password-reset")
  @RateLimit(
      bucket = "passwordReset",
      key = RateLimitKeyType.SPEL,
      keyExpression = "#request.email()")
  @RateLimit(bucket = "passwordResetIp", key = RateLimitKeyType.IP)
  public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    authenticationService.resetPassword(request.email());
    return ResponseEntity.ok().build();
  }

  @PutMapping("/password-reset")
  public ResponseEntity<Void> confirmPasswordReset(
      @Valid @RequestBody ConfirmPasswordResetRequest request) {
    authenticationService.confirmPasswordReset(request.token(), request.newPassword());
    return ResponseEntity.ok().build();
  }
}

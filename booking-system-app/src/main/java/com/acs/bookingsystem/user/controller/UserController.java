package com.acs.bookingsystem.user.controller;

import com.acs.bookingsystem.security.CurrentUser;
import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.model.UserProfile;
import com.acs.bookingsystem.user.request.UpdateUserInfoRequest;
import com.acs.bookingsystem.user.request.UpdateUserRequest;
import com.acs.bookingsystem.user.response.AuthenticateResponse;
import com.acs.bookingsystem.user.response.UserStatusResponse;
import com.acs.bookingsystem.user.service.AuthenticationService;
import com.acs.bookingsystem.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
@Validated
public class UserController {

  private final UserService userService;
  private final AuthenticationService authenticationService;

  @GetMapping
  public ResponseEntity<UserProfile> getUserProfile(@CurrentUser User user) {
    return ResponseEntity.ok(userService.getUserProfile(user));
  }

  @PatchMapping
  public ResponseEntity<UserProfile> updateUserInfo(
      @CurrentUser User user, @Valid @RequestBody UpdateUserInfoRequest request) {
    return ResponseEntity.ok(userService.updateUserInfo(user, request));
  }

  @PutMapping("/credentials")
  public ResponseEntity<AuthenticateResponse> updateUserCredentials(
      @CurrentUser User user, @Valid @RequestBody UpdateUserRequest request) {
    return ResponseEntity.ok(authenticationService.updateUserCredentials(user, request));
  }

  @PatchMapping("/status")
  public ResponseEntity<UserStatusResponse> disableUser(@CurrentUser User user) {
    return ResponseEntity.ok(userService.disableUser(user));
  }
}

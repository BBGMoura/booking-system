package com.acs.bookingsystem.user.controller;

import com.acs.bookingsystem.security.CurrentUser;
import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.model.UserProfile;
import com.acs.bookingsystem.user.request.InviteRequest;
import com.acs.bookingsystem.user.request.UpdateUserStatusRequest;
import com.acs.bookingsystem.user.response.InviteResponse;
import com.acs.bookingsystem.user.response.UserStatusResponse;
import com.acs.bookingsystem.user.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@SuppressWarnings("java:S4684") // @CurrentUser resolves from security context, not request body
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Validated
public class UserAdminController {

  private final UserService userService;

  @GetMapping("/{userUid}")
  public ResponseEntity<UserProfile> getUser(@PathVariable UUID userUid) {
    return ResponseEntity.ok(userService.getUserProfileByUid(userUid));
  }

  @GetMapping
  public ResponseEntity<Page<UserProfile>> getUsers(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size) {
    return ResponseEntity.ok(userService.getUserProfiles(page, size));
  }

  @PostMapping("/invite")
  public ResponseEntity<InviteResponse> inviteUser(@Valid @RequestBody InviteRequest request) {
    return ResponseEntity.ok(userService.invite(request));
  }

  @PatchMapping("/{userUid}")
  public ResponseEntity<UserStatusResponse> updateUserStatus(
      @CurrentUser User admin,
      @PathVariable UUID userUid,
      @Valid @RequestBody UpdateUserStatusRequest request) {
    UserStatusResponse response =
        request.enabled()
            ? userService.enableUser(userUid)
            : userService.disableUser(userUid, admin);
    return ResponseEntity.ok(response);
  }
}

package com.acs.bookingsystem.user.controller;

import com.acs.bookingsystem.user.model.UserProfile;
import com.acs.bookingsystem.user.request.InviteRequest;
import com.acs.bookingsystem.user.response.InviteResponse;
import com.acs.bookingsystem.user.response.UserStatusResponse;
import com.acs.bookingsystem.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Validated
public class UserAdminController {

    private final UserService userService;

    @GetMapping("/users/{userUid}")
    public ResponseEntity<UserProfile> getUser(@PathVariable UUID userUid) {
        return ResponseEntity.ok(userService.getUserProfileByUid(userUid));
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserProfile>> getUsers(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(userService.getUserProfiles(page, size));
    }

    @PostMapping("/users/invite")
    public ResponseEntity<InviteResponse> inviteUser(@Valid @RequestBody InviteRequest request) {
        return ResponseEntity.ok(userService.invite(request));
    }

    @PatchMapping("/users/{userUid}/status")
    public ResponseEntity<UserStatusResponse> updateUserStatus(@PathVariable UUID userUid,
                                                               @RequestParam boolean enable) {
        return ResponseEntity.ok(userService.updateEnableStatusByUid(userUid, enable));
    }
}

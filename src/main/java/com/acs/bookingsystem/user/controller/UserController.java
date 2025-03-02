package com.acs.bookingsystem.user.controller;

import com.acs.bookingsystem.common.security.CurrentUser;
import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.model.UserProfile;
import com.acs.bookingsystem.user.request.UpdateUserRequest;
import com.acs.bookingsystem.user.response.UserStatusResponse;
import com.acs.bookingsystem.user.service.AuthenticateService;
import com.acs.bookingsystem.user.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@AllArgsConstructor
@Validated
public class UserController {
    private AuthenticateService authenticateService;
    private UserProfileService userProfileService;

    @GetMapping
    public ResponseEntity<UserProfile> getUserProfile(@CurrentUser User user) {
        return ResponseEntity.ok(userProfileService.getUserProfile(user.getId()));
    }

    @PutMapping
    public ResponseEntity<Void> updateUserCredentials(@CurrentUser User user,
                                                      @Valid @RequestBody UpdateUserRequest request) {
        authenticateService.updateUserCredentials(user.getId(), request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/disable")
    public ResponseEntity<UserStatusResponse> disableUser(@CurrentUser User user) {
        return ResponseEntity.ok( authenticateService.updatedEnabledStatus(user.getId(), false));
    }
}

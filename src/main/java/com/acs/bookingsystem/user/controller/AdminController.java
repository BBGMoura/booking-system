package com.acs.bookingsystem.user.controller;

import com.acs.bookingsystem.user.model.UserProfile;
import com.acs.bookingsystem.user.request.InviteRequest;
import com.acs.bookingsystem.user.response.InvitationResponse;
import com.acs.bookingsystem.user.service.AuthenticateService;
import com.acs.bookingsystem.user.service.UserProfileService;
import com.acs.bookingsystem.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Validated
public class AdminController {
    private AuthenticateService authenticateService;
    private UserService userService;
    private UserProfileService userProfileService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserProfile> getUser(@PathVariable int userId) {
        return ResponseEntity.ok(userProfileService.getUserProfile(userId));
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserProfile>> getUsers(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(userProfileService.getUserProfiles(page, size));
    }

    @PostMapping("/user/invite")
    public ResponseEntity<InvitationResponse> inviteUser(@Valid @RequestBody InviteRequest request) {
        return ResponseEntity.ok(authenticateService.invite(request));
    }

    @PutMapping("/user/{userId}/status")
    public ResponseEntity<Void> updateUserStatus(@PathVariable int userId,
                                                 @RequestParam boolean enable){
        authenticateService.updatedEnabledStatus(userId,enable);
        return ResponseEntity.noContent().build();
    }
}

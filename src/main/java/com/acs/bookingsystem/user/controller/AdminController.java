package com.acs.bookingsystem.user.controller;

import com.acs.bookingsystem.user.model.UserProfile;
import com.acs.bookingsystem.user.request.InviteRequest;
import com.acs.bookingsystem.user.response.InvitationResponse;
import com.acs.bookingsystem.user.service.AuthenticationService;
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
    private AuthenticationService authenticationService;
    private UserService userService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserProfile> getUser(@PathVariable int userId) {
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserProfile>> getUsers(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(userService.getUserProfiles(page, size));
    }

    @PostMapping("/user/invite")
    public ResponseEntity<InvitationResponse> inviteUser(@Valid @RequestBody InviteRequest request) {
        return ResponseEntity.ok(authenticationService.invite(request));
    }

    @PutMapping("/user/{userId}/status")
    public ResponseEntity<Void> updateUserStatus(@PathVariable int userId,
                                                 @RequestParam boolean enable){
        userService.updateUserStatus(userId,enable);
        return ResponseEntity.noContent().build();
    }
}

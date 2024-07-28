package com.acs.bookingsystem.user.controller;

import com.acs.bookingsystem.common.security.CurrentUser;
import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.model.UserProfile;
import com.acs.bookingsystem.user.request.UpdateUserCredentialRequest;
import com.acs.bookingsystem.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Validated
public class UserController {
    private UserService userService;

    @GetMapping
    public ResponseEntity<UserProfile> getUserProfile(@CurrentUser User user) {
        return ResponseEntity.ok(userService.getUserProfile(user.getId()));
    }

    @PutMapping
    public ResponseEntity<Void> updateUserCredentials(@CurrentUser User user,
                                                      @Valid @RequestBody UpdateUserCredentialRequest request) {
        userService.updateUser(user.getId(),request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/disable")
    public ResponseEntity<Void> disableUser(@CurrentUser User user){
        userService.updateUserStatus(user.getId(), false);
        return ResponseEntity.noContent().build();
    }
}

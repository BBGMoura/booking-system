package com.acs.bookingsystem.user.controller;

import com.acs.bookingsystem.user.request.ResetPasswordRequest;
import com.acs.bookingsystem.user.service.AuthenticateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("password")
@RequiredArgsConstructor
@Validated
public class ResetPasswordController {
    private final AuthenticateService authenticateService;

    @PatchMapping("/password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authenticateService.resetPassword(request.email());
        return ResponseEntity.noContent().build();
    }
}

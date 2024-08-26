package com.acs.bookingsystem.user.controller;

import com.acs.bookingsystem.user.request.AuthenticateRequest;
import com.acs.bookingsystem.user.request.RegisterRequest;
import com.acs.bookingsystem.user.response.AuthenticateResponse;
import com.acs.bookingsystem.user.response.RegistrateResponse;
import com.acs.bookingsystem.user.service.AuthenticateService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
@AllArgsConstructor
@Validated
public class AuthenticationController {
    private AuthenticateService authenticateService;

    @PostMapping("/register")
    public ResponseEntity<RegistrateResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authenticateService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticateResponse> authenticate(@Valid @RequestBody AuthenticateRequest request) {
        return ResponseEntity.ok(authenticateService.authenticate(request));
    }
}

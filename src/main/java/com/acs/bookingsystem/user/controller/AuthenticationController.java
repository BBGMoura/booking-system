package com.acs.bookingsystem.user.controller;

import com.acs.bookingsystem.user.request.AuthenticationRequest;
import com.acs.bookingsystem.user.request.RegisterRequest;
import com.acs.bookingsystem.user.response.AuthenticationResponse;
import com.acs.bookingsystem.user.response.RegistrationResponse;
import com.acs.bookingsystem.user.service.AuthenticateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@Validated
public class AuthenticationController {
    private AuthenticateService authenticateService;

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authenticateService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticateService.authenticate(request));
    }
}

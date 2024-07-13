package com.acs.bookingsystem.authorization.controller;

import com.acs.bookingsystem.authorization.request.AuthenticationRequest;
import com.acs.bookingsystem.authorization.request.RegisterRequest;
import com.acs.bookingsystem.authorization.response.AuthenticationResponse;
import com.acs.bookingsystem.authorization.response.RegistrationResponse;
import com.acs.bookingsystem.authorization.service.AuthenticationService;
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
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PatchMapping("/reset-password/{email}")
    public ResponseEntity<Void> resetPassword(@PathVariable String email) {
        authenticationService.resetPassword(email);
        return ResponseEntity.noContent().build();
    }
}

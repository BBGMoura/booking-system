package com.acs.bookingsystem.user.controller;

import com.acs.bookingsystem.user.request.AuthenticateRequest;
import com.acs.bookingsystem.user.request.RegisterRequest;
import com.acs.bookingsystem.user.response.AuthenticateResponse;
import com.acs.bookingsystem.user.response.CheckInviteResponse;
import com.acs.bookingsystem.user.response.RegisterResponse;
import com.acs.bookingsystem.user.service.AuthenticateService;
import com.acs.bookingsystem.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
@AllArgsConstructor
@Validated
public class AuthenticationController {

    private final AuthenticateService authenticateService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authenticateService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticateResponse> authenticate(@Valid @RequestBody AuthenticateRequest request) {
        return ResponseEntity.ok(authenticateService.authenticate(request));
    }

    @GetMapping("/check-invite")
    public ResponseEntity<CheckInviteResponse> checkInvite(@RequestParam @Email String email) {
        boolean invited = userService.isEmailInvited(email);
        return ResponseEntity.ok(new CheckInviteResponse(email, invited));
    }
}

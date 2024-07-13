package com.acs.bookingsystem.admin.controller;

import com.acs.bookingsystem.admin.response.InvitationResponse;
import com.acs.bookingsystem.authorization.enums.Permission;
import com.acs.bookingsystem.authorization.service.AuthenticationService;
import com.acs.bookingsystem.authorization.service.AuthenticationServiceImpl;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("admin")
@RequiredArgsConstructor
public class AdminController {
    private final AuthenticationService authenticationService;

    @PostMapping("/invite")
    public ResponseEntity<InvitationResponse> invite(@NotBlank
                                                     @Size(max = 254)
                                                     @Pattern(regexp = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", message = "Invalid email format")
                                                     @RequestParam String email,
                                                     @RequestParam Permission permission) {
        return ResponseEntity.ok(authenticationService.inviteUser(email, permission));
    }

    @PatchMapping("/set-enabled-status")
    public ResponseEntity<Void> setEnabledStatus(@RequestParam String email,
                                                 @RequestParam boolean enabled) {
        authenticationService.setEnabledStatus(email, enabled);
        return ResponseEntity.noContent().build();
    }
}

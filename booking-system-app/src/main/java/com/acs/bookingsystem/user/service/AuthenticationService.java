package com.acs.bookingsystem.user.service;

import com.acs.bookingsystem.common.email.EmailService;
import com.acs.bookingsystem.security.util.JwtUtil;
import com.acs.bookingsystem.security.util.PasswordUtil;
import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.request.AuthenticateRequest;
import com.acs.bookingsystem.user.request.RegisterRequest;
import com.acs.bookingsystem.user.request.UpdateUserRequest;
import com.acs.bookingsystem.user.response.AuthenticateResponse;
import com.acs.bookingsystem.user.response.RegisterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordUtil passwordUtil;
    private final EmailService emailService;

    public AuthenticateResponse authenticate(AuthenticateRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        UserDetails user = (UserDetails) authentication.getPrincipal();
        return AuthenticateResponse.builder().token(jwtUtil.generateToken(user)).build();
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String encodedPassword = passwordUtil.encodePassword(request.password());
        User user = userService.registerUser(request, encodedPassword);
        return RegisterResponse.builder()
                               .token(jwtUtil.generateToken(user))
                               .uid(user.getUid())
                               .firstName(user.getFirstName())
                               .lastName(user.getLastName())
                               .email(user.getEmail())
                               .phoneNumber(user.getPhoneNumber())
                               .role(user.getRole())
                               .enabled(user.isEnabled())
                               .build();
    }

    @Transactional
    public AuthenticateResponse updateUserCredentials(User user, UpdateUserRequest request) {
        String encodedPassword = request.password() != null
                ? passwordUtil.encodePassword(request.password())
                : null;
        User updatedUser = userService.updateUserCredentials(user, request.email(), encodedPassword);
        return AuthenticateResponse.builder().token(jwtUtil.generateToken(updatedUser)).build();
    }

    public void resetPassword(String email) {
        String newPassword = passwordUtil.generateNewPassword();
        userService.resetPassword(email, passwordUtil.encodePassword(newPassword));
        emailService.sendPasswordResetEmail(email, newPassword);
    }
}

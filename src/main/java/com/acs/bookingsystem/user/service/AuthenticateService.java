package com.acs.bookingsystem.user.service;

import com.acs.bookingsystem.booking.service.BookingManagerService;
import com.acs.bookingsystem.user.entity.UserInfo;
import com.acs.bookingsystem.user.request.AuthenticateRequest;
import com.acs.bookingsystem.user.request.InviteRequest;
import com.acs.bookingsystem.user.request.RegisterRequest;
import com.acs.bookingsystem.user.request.UpdateUserRequest;
import com.acs.bookingsystem.user.response.InviteResponse;
import com.acs.bookingsystem.user.response.AuthenticateResponse;
import com.acs.bookingsystem.user.response.RegisterResponse;
import com.acs.bookingsystem.common.email.EmailService;
import com.acs.bookingsystem.security.util.JwtUtil;
import com.acs.bookingsystem.security.util.PasswordUtil;
import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.response.UserStatusResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@AllArgsConstructor
@Validated
public class AuthenticateService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UserInfoService userInfoService;
    private final UserService userService;
    private final BookingManagerService bookingManagerService;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final PasswordUtil passwordUtil;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public InviteResponse invite(InviteRequest request) {
        final User user = userService.createUser(request.email(), request.role());

        emailService.sendInvitationEmail(request.email());

        return InviteResponse.builder()
                               .userId(user.getId())
                               .email(user.getEmail())
                               .role(user.getRole())
                               .build();
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String encodedPassword = passwordUtil.encodePassword(request.password());

        final User user = userService.registerUser(request.email(), encodedPassword);

        UserInfo userInfo = UserInfo.builder()
                                    .firstName(request.firstName())
                                    .lastName(request.lastName())
                                    .phoneNumber(request.phoneNumber())
                                    .user(user)
                                    .build();
        userInfo = userInfoService.createUserInfo(userInfo);

        final String jwtToken = jwtUtil.generateToken(user);

        return RegisterResponse.builder()
                                 .token(jwtToken)
                                 .userId(user.getId())
                                 .firstName(userInfo.getFirstName())
                                 .lastName(userInfo.getLastName())
                                 .email(user.getEmail())
                                 .phoneNumber(userInfo.getPhoneNumber())
                                 .userInfoId(userInfo.getId())
                                 .role(user.getRole())
                                 .enabled(user.isEnabled())
                                 .build();
    }

    public AuthenticateResponse authenticate(AuthenticateRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(),
                                                        request.password())
        );
        final UserDetails user = userDetailsService.loadUserByUsername(request.email());

        final String jwtToken = jwtUtil.generateToken(user);

        return AuthenticateResponse.builder().token(jwtToken).build();
    }

    @Transactional
    public void updateUserCredentials(int userId, UpdateUserRequest request) {
        String encodedPassword = passwordUtil.encodePassword(request.password());

        User user = userService.updateUserCredentials(userId, request.email(), encodedPassword);

        String jwtToken = jwtUtil.generateToken(user);

        AuthenticateResponse.builder().token(jwtToken).build();
    }

    @Transactional
    public UserStatusResponse updatedEnabledStatus(int userId, boolean enabled) {
        User user = userService.updateEnableStatus(userId, enabled);

        if (!user.isEnabled()) {
            bookingManagerService.deactivateAllBookingsByUserId(userId);
        }

        return UserStatusResponse.builder()
                                 .userId(user.getId())
                                 .enabled(user.getEnabled())
                                 .build();
    }

    public void resetPassword(String email) {
        final String newPassword = passwordUtil.generateNewPassword();

        String encodedPassword = passwordUtil.encodePassword(newPassword);

        userService.resetPassword(email, encodedPassword);

        emailService.sendPasswordResetEmail(email, newPassword);
    }
}

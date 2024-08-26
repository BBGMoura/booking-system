package com.acs.bookingsystem.user.service.impl;

import com.acs.bookingsystem.user.entity.UserInfo;
import com.acs.bookingsystem.user.request.*;
import com.acs.bookingsystem.user.response.InvitateResponse;
import com.acs.bookingsystem.user.response.AuthenticateResponse;
import com.acs.bookingsystem.user.response.RegistrateResponse;
import com.acs.bookingsystem.common.email.EmailUtil;
import com.acs.bookingsystem.common.security.util.JwtUtil;
import com.acs.bookingsystem.common.security.util.PasswordUtil;
import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.response.UserStatusResponse;
import com.acs.bookingsystem.user.service.AuthenticateService;
import com.acs.bookingsystem.user.service.UserInfoService;
import com.acs.bookingsystem.user.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.annotation.Secured;
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
public class AuthenticateServiceImpl implements AuthenticateService {
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UserInfoService userInfoService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final EmailUtil emailUtil;
    private final PasswordUtil passwordUtil;


    @Override
    @Secured("ADMIN")
    public InvitateResponse invite(InviteRequest request) {
        final User user = userService.createUser(request.email(), request.permission());

        emailUtil.sendInvitationEmail(request.email());

        return InvitateResponse.builder()
                               .userId(user.getId())
                               .email(user.getEmail())
                               .permission(user.getPermission())
                               .build();
    }

    @Override
    @Transactional
    public RegistrateResponse register(RegisterRequest request) {
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

        return RegistrateResponse.builder()
                                 .token(jwtToken)
                                 .userId(user.getId())
                                 .firstName(userInfo.getFirstName())
                                 .lastName(userInfo.getLastName())
                                 .email(user.getEmail())
                                 .phoneNumber(userInfo.getPhoneNumber())
                                 .userInfoId(userInfo.getId())
                                 .permission(user.getPermission())
                                 .enabled(user.isEnabled())
                                 .build();
    }

    @Override
    public AuthenticateResponse authenticate(AuthenticateRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(),
                                                        request.password())
        );
        final UserDetails user = userDetailsService.loadUserByUsername(request.email());

        final String jwtToken = jwtUtil.generateToken(user);

        return AuthenticateResponse.builder().token(jwtToken).build();
    }

    @Override
    public void updateUserCredentials(int userId, UpdateUserRequest request) {
        String encodedPassword = passwordUtil.encodePassword(request.password());

        User user = userService.updateUserCredentials(userId, request.email(), encodedPassword);

        String jwtToken = jwtUtil.generateToken(user);

        AuthenticateResponse.builder().token(jwtToken).build();
    }

    @Override
    public UserStatusResponse updatedEnabledStatus(int userId, boolean enabled) {
        User user = userService.updateEnableStatus(userId, enabled);

        return UserStatusResponse.builder()
                                 .userId(user.getId())
                                 .enabled(user.getEnabled())
                                 .build();
    }

    @Override
    public void resetPassword(String email) {
        final String newPassword = passwordUtil.generateNewPassword();

        String encodedPassword = passwordUtil.encodePassword(newPassword);

        userService.resetPassword(email, encodedPassword);

        emailUtil.sendPasswordResetEmail(email, newPassword);
    }
}

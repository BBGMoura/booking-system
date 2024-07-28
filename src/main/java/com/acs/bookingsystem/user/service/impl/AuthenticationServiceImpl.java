package com.acs.bookingsystem.user.service.impl;

import com.acs.bookingsystem.user.entity.UserInfo;
import com.acs.bookingsystem.user.request.InviteRequest;
import com.acs.bookingsystem.user.response.InvitationResponse;
import com.acs.bookingsystem.user.request.AuthenticationRequest;
import com.acs.bookingsystem.user.request.RegisterRequest;
import com.acs.bookingsystem.user.response.AuthenticationResponse;
import com.acs.bookingsystem.user.response.RegistrationResponse;
import com.acs.bookingsystem.common.email.EmailUtil;
import com.acs.bookingsystem.common.exception.RequestException;
import com.acs.bookingsystem.common.exception.model.ErrorCode;
import com.acs.bookingsystem.common.security.util.JwtUtil;
import com.acs.bookingsystem.common.security.util.PasswordUtil;
import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.repository.UserRepository;
import com.acs.bookingsystem.user.service.AuthenticationService;
import com.acs.bookingsystem.user.service.UserInfoService;
import com.acs.bookingsystem.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
@PropertySource("classpath:email.properties")
public class AuthenticationServiceImpl implements AuthenticationService {
    private UserRepository userRepository;
    private UserDetailsService userDetailsService;
    private PasswordEncoder passwordEncoder;
    private JwtUtil jwtUtil;
    private AuthenticationManager authenticationManager;
    private UserService userService;
    private EmailUtil emailUtil;
    private PasswordUtil passwordUtil;
    private UserInfoService userInfoService;

    @Override
    @Secured("ADMIN")
    public InvitationResponse invite(InviteRequest request) {
        userRepository.findByEmail(request.email())
                      .ifPresent(authUser -> {
                              throw new RequestException("User with email: " + request.email() + " is already invited.",
                                                         ErrorCode.INVALID_INVITATION_REQUEST);
                          });

        final User user = User.builder()
                              .email(request.email())
                              .permission(request.permission())
                              .build();
        userRepository.save(user);

        emailUtil.sendInvitationEmail(request.email());

        return InvitationResponse.builder()
                                 .userId(user.getId())
                                 .email(user.getEmail())
                                 .permission(user.getPermission())
                                 .build();
    }

    @Override
    @Transactional
    public RegistrationResponse register(RegisterRequest request) {
         User user = userRepository.findByEmail(request.email())
                                   .orElseThrow(() -> new RequestException("Email " + request.email() + " has not been invited to register for the booking system.",
                                                                                            ErrorCode.INVALID_REGISTRATION_REQUEST));

        user.setPassword(passwordEncoder.encode(request.password()));
        user.setLocked(false);
        user.setEnabled(true);
        user = userService.createUser(user);

        UserInfo userInfo = UserInfo.builder()
                                    .firstName(request.firstName())
                                    .lastName(request.lastName())
                                    .phoneNumber(request.phoneNumber())
                                    .user(user)
                                    .build();
        userInfo = userInfoService.createUserInfo(userInfo);

        final String jwtToken = jwtUtil.generateToken(user);
        return RegistrationResponse.builder()
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
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(),
                                                        request.password())
        );
        final UserDetails user = userDetailsService.loadUserByUsername(request.email());
        final String jwtToken = jwtUtil.generateToken(user);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }
}

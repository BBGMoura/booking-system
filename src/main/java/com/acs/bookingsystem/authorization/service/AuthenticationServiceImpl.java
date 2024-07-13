package com.acs.bookingsystem.authorization.service;

import com.acs.bookingsystem.admin.response.InvitationResponse;
import com.acs.bookingsystem.common.security.util.JwtUtil;
import com.acs.bookingsystem.authorization.entity.AuthUser;
import com.acs.bookingsystem.authorization.enums.Permission;
import com.acs.bookingsystem.authorization.repository.AuthUserRepository;
import com.acs.bookingsystem.authorization.request.AuthenticationRequest;
import com.acs.bookingsystem.authorization.request.RegisterRequest;
import com.acs.bookingsystem.authorization.response.AuthenticationResponse;
import com.acs.bookingsystem.authorization.response.RegistrationResponse;
import com.acs.bookingsystem.common.security.util.PasswordUtil;
import com.acs.bookingsystem.common.email.EmailUtil;
import com.acs.bookingsystem.common.exception.AuthorizationException;
import com.acs.bookingsystem.common.exception.RequestException;
import com.acs.bookingsystem.common.exception.model.ErrorCode;
import com.acs.bookingsystem.user.dto.UserDTO;
import com.acs.bookingsystem.user.entities.User;
import com.acs.bookingsystem.user.mapper.UserMapper;
import com.acs.bookingsystem.user.request.UserRequest;
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
    private final AuthUserRepository authUserRepository;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final UserMapper userMapper;
    private final EmailUtil emailUtil;
    private final PasswordUtil passwordUtil;

    @Override
    @Secured("ADMIN")
    public InvitationResponse inviteUser(String email, Permission permission) {
        authUserRepository.findByEmail(email)
                          .ifPresent(authUser -> {
                              throw new RequestException("User with email: " + email + " is already invited.", ErrorCode.INVALID_INVITATION_REQUEST);
                          });

        final AuthUser authUser = AuthUser.builder()
                                          .email(email)
                                          .permission(permission)
                                          .build();
        authUserRepository.save(authUser);

        emailUtil.sendInvitationEmail(email);
        return InvitationResponse.builder()
                                 .authId(authUser.getId())
                                 .email(authUser.getEmail())
                                 .permission(authUser.getPermission())
                                 .build();
    }

    @Override
    public RegistrationResponse register(RegisterRequest request) {
         AuthUser authUser = authUserRepository.findByEmail(request.email())
                                                    .orElseThrow(() -> new RequestException("Email " + request.email() + " has not been invited to register for the booking system.",
                                                                                            ErrorCode.INVALID_REGISTRATION_REQUEST));

        final User user = createUser(request);
        authUser.setPassword(passwordEncoder.encode(request.password()));
        authUser.setLocked(false);
        authUser.setEnabled(true);
        authUser.setUser(user);
        authUserRepository.save(authUser);
        userService.saveAuthToUser(user.getId(), authUser);

        final String jwtToken = jwtUtil.generateToken(authUser);
        return RegistrationResponse.builder()
                                   .token(jwtToken)
                                   .userId(user.getId())
                                   .firstName(user.getFirstName())
                                   .lastName(user.getLastName())
                                   .email(authUser.getEmail())
                                   .phoneNumber(user.getPhoneNumber())
                                   .authUserId(authUser.getId())
                                   .permission(authUser.getPermission())
                                   .enabled(authUser.isEnabled())
                                   .build();
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(),
                                                        request.password())
        );
        final UserDetails authUser = userDetailsService.loadUserByUsername(request.email());
        final String jwtToken = jwtUtil.generateToken(authUser);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }

    @Override
    public void verifyUser(String token, int userId) {
        String email = jwtUtil.extractUsername(token.substring(7));
        AuthUser authUser = getAuthUserByEmail(email);
        if (userId != authUser.getUser().getId()) {
            throw new AuthorizationException("User "+ email + " does not match authorization token.", ErrorCode.AUTHENTICATION_ERROR);
        }
    }

    @Override
    @Transactional
    public String updateEmail(String token, int userId, String updatedEmail) {
        verifyUser(token, userId);

        authUserRepository.findByEmail(updatedEmail)
                          .ifPresent(existingUser -> {
                              throw new RequestException("Cannot update email address as user with email: " + updatedEmail + " already exists.",
                                                         ErrorCode.INVALID_UPDATE_REQUEST);
                          });

        String email = jwtUtil.extractUsername(token.substring(7));

        AuthUser authUser = getAuthUserByEmail(email);
        authUser.setEmail(updatedEmail);
        authUserRepository.save(authUser);

        userService.updateUserEmail(userId,updatedEmail);
        return authUser.getUsername();
    }

    @Override
    public void disableUserByToken(String token) {
        final String email = jwtUtil.extractUsername(token.substring(7));
        setEnabledStatus(email, false);
    }
    
    @Override
    public void setEnabledStatus(String email, boolean enabled){
        final AuthUser authUser = getAuthUserByEmail(email);
        if (authUser.isEnabled() == enabled) {
            throw new RequestException("User status is already " + enabled, ErrorCode.USER_ERROR);
        }
        authUser.setEnabled(enabled);
        authUserRepository.save(authUser);
    }

    @Override
    public void changeUserPasswordByToken(String token, String password) {
        final String email = jwtUtil.extractUsername(token.substring(7));
        final AuthUser authUser = getAuthUserByEmail(email);
        authUser.setPassword(passwordEncoder.encode(password));
        authUserRepository.save(authUser);
    }

    @Override
    public void resetPassword(String email) {
        final AuthUser authUser = getAuthUserByEmail(email);
        final String newPassword = passwordUtil.generateNewPassword();

        authUser.setPassword(newPassword);
        authUserRepository.save(authUser);

        emailUtil.sendPasswordResetEmail(email,newPassword);
    }

    private AuthUser getAuthUserByEmail(String email) {
        return authUserRepository.findByEmail(email)
                                 .orElseThrow(() -> new RequestException("User with email: " + email + " does not exist.",
                                                                         ErrorCode.USER_ERROR));
    }

    private User createUser(RegisterRequest request) {
        final UserRequest userRequest = UserRequest.builder()
                                                   .firstName(request.firstName())
                                                   .lastName(request.lastName())
                                                   .email(request.email())
                                                   .phoneNumber(request.phoneNumber())
                                                   .build();

        UserDTO userDto = userService.createUser(userRequest);
        return userMapper.mapDTOToUser(userDto);
    }
}

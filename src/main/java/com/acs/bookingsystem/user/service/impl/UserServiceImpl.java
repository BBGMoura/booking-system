package com.acs.bookingsystem.user.service.impl;

import com.acs.bookingsystem.common.exception.RequestException;
import com.acs.bookingsystem.common.exception.model.ErrorCode;
import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.enums.Permission;
import com.acs.bookingsystem.user.repository.UserRepository;
import com.acs.bookingsystem.user.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public User getUserById(int userId) {
        return userRepository.findById(userId)
                             .orElseThrow(() ->
                                     new RequestException("Cannot find user. Please contact support.",
                                             ErrorCode.INVALID_USER_ID));
    }

    @Override
    public Page<User> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public User createUser(String email, Permission permission) {
        userRepository.findByEmail(email)
                      .ifPresent(user -> {
                                    throw new RequestException("User with email: " + email + " is already invited.",
                                            ErrorCode.INVALID_INVITATION_REQUEST);
                      });

        final User user = User.builder()
                                .email(email)
                                .permission(permission)
                                .build();
        return userRepository.save(user);
    }

    @Override
    public User registerUser(String email, String password) {
        User user = userRepository.findByEmail(email)
                                  .orElseThrow(() ->
                                            new RequestException("Email " + email + " has not been invited to register for the booking system.",
                                                    ErrorCode.INVALID_REGISTRATION_REQUEST));

        user.setPassword(password);
        user.setLocked(false);
        user.setEnabled(true);
        return userRepository.save(user);
   }

    public User updateUserCredentials(int userId, String email, String password) {
        User user = getUserById(userId);

        if (email != null && !email.isBlank()) {
            user.setEmail(email);
        }
        if (password != null && !password.isBlank()) {
            user.setPassword(password);
        }
        return userRepository.save(user);
    }

    public User updateEnableStatus(int userId, boolean enabled) {
        User user = getUserById(userId);
        user.setEnabled(enabled);
        return userRepository.save(user);
    }

    @Override
    public void resetPassword(String email, String password) {
        final User user = userRepository.findByEmail(email)
                                        .orElseThrow(() ->
                                                new RequestException("Cannot find user with email " + email,
                                                        ErrorCode.USER_ERROR));
        user.setPassword(password);
    }
}

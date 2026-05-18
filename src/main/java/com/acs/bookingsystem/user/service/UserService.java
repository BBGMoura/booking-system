package com.acs.bookingsystem.user.service;

import com.acs.bookingsystem.booking.service.BookingService;
import com.acs.bookingsystem.common.exception.RequestException;
import com.acs.bookingsystem.common.exception.NotFoundException;
import com.acs.bookingsystem.common.exception.model.ErrorCode;
import com.acs.bookingsystem.common.email.EmailService;
import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.model.UserProfile;
import com.acs.bookingsystem.user.repository.UserRepository;
import com.acs.bookingsystem.user.request.InviteRequest;
import com.acs.bookingsystem.user.request.RegisterRequest;
import com.acs.bookingsystem.user.request.UpdateUserInfoRequest;
import com.acs.bookingsystem.user.response.InviteResponse;
import com.acs.bookingsystem.user.response.UserStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BookingService bookingService;
    private final EmailService emailService;

    public User getUserById(int userId) {
        return userRepository.findById(userId)
                             .orElseThrow(() -> new RequestException(
                                     "Cannot find user with id " + userId,
                                     ErrorCode.INVALID_USER_ID));
    }

    public User getUserByUid(UUID uid) {
        return userRepository.findByUid(uid)
                             .orElseThrow(() -> new NotFoundException(
                                     "Cannot find user " + uid,
                                     ErrorCode.INVALID_USER_ID));
    }

    public UserProfile getUserProfile(User user) {
        return toProfile(user);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public UserProfile getUserProfileByUid(UUID uid) {
        return toProfile(getUserByUid(uid));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Page<UserProfile> getUserProfiles(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size))
                             .map(this::toProfile);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public InviteResponse invite(InviteRequest request) {
        userRepository.findByEmail(request.email())
                      .ifPresent(u -> { throw new RequestException(
                              "User with email " + request.email() + " is already invited.",
                              ErrorCode.INVALID_INVITATION_REQUEST); });

        User user = userRepository.save(User.builder()
                                            .email(request.email())
                                            .role(request.role())
                                            .build());

        emailService.sendInvitationEmail(request.email());

        return InviteResponse.builder()
                             .uid(user.getUid())
                             .email(user.getEmail())
                             .role(user.getRole())
                             .build();
    }

    public User registerUser(RegisterRequest request, String encodedPassword) {
        User user = userRepository.findByEmail(request.email())
                                  .orElseThrow(() -> new RequestException(
                                          "Email " + request.email() + " has not been invited.",
                                          ErrorCode.INVALID_REGISTRATION_REQUEST));

        user.setPassword(encodedPassword);
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhoneNumber(request.phoneNumber());
        user.setLocked(false);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    @Transactional
    public UserProfile updateUserInfo(User user, UpdateUserInfoRequest request) {
        if (request.firstName() != null && !request.firstName().isBlank()) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null && !request.lastName().isBlank()) {
            user.setLastName(request.lastName());
        }
        if (request.phoneNumber() != null && !request.phoneNumber().isBlank()) {
            user.setPhoneNumber(request.phoneNumber());
        }

        return toProfile(userRepository.save(user));
    }

    public User updateUserCredentials(User user, String email, String encodedPassword) {
        if (email != null && !email.isBlank()) {
            user.setEmail(email);
        }
        if (encodedPassword != null && !encodedPassword.isBlank()) {
            user.setPassword(encodedPassword);
        }
        return userRepository.save(user);
    }

    @Transactional
    public UserStatusResponse disableUser(User user) {
        return applyDisable(user);
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public UserStatusResponse enableUser(UUID uid) {
        User user = getUserByUid(uid);
        user.setEnabled(true);
        userRepository.save(user);
        return UserStatusResponse.builder()
                                 .uid(user.getUid())
                                 .enabled(true)
                                 .build();
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public UserStatusResponse disableUser(UUID uid) {
        return applyDisable(getUserByUid(uid));
    }

    private UserStatusResponse applyDisable(User user) {
        user.setEnabled(false);
        userRepository.save(user);
        bookingService.deactivateAllBookingsByUserId(user.getId());
        return UserStatusResponse.builder()
                                 .uid(user.getUid())
                                 .enabled(false)
                                 .build();
    }

    public void resetPassword(String email, String encodedPassword) {
        User user = userRepository.findByEmail(email)
                                  .orElseThrow(() -> new RequestException(
                                          "Cannot find user with email " + email,
                                          ErrorCode.USER_ERROR));
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }

    public boolean isEmailInvited(String email) {
        return userRepository.findByEmail(email)
                             .map(user -> user.getPassword() == null)
                             .orElse(false);
    }

    private UserProfile toProfile(User user) {
        return UserProfile.builder()
                          .uid(user.getUid())
                          .firstName(user.getFirstName())
                          .lastName(user.getLastName())
                          .email(user.getEmail())
                          .phoneNumber(user.getPhoneNumber())
                          .enabled(user.isEnabled())
                          .role(user.getRole())
                          .build();
    }
}

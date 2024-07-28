package com.acs.bookingsystem.user.service;

import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.model.UserProfile;
import com.acs.bookingsystem.user.request.UpdateUserCredentialRequest;
import org.springframework.data.domain.Page;

public interface UserService {
    User createUser(User user);
    UserProfile getUserProfile(int userId);
    Page<UserProfile> getUserProfiles(int page, int size);
    void updateUser(int userId, UpdateUserCredentialRequest request);
    void updateUserStatus(int userId, boolean enable);
    void resetPassword(String email);
}

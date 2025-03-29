package com.acs.bookingsystem.user.service;

import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    User getUserById(int userId);
    User createUser(String email, Role role);
    User registerUser(String email, String password);
    User updateUserCredentials(int userId, String email, String password);
    User updateEnableStatus(int userId, boolean enabled);
    void resetPassword(String email, String password);
    Page<User> getUsers(Pageable pageable);
}

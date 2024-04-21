package com.acs.bookingsystem.user.service;

import com.acs.bookingsystem.user.dto.UserDTO;
import com.acs.bookingsystem.user.request.UserRegistrationRequest;
import com.acs.bookingsystem.user.request.UserUpdateRequest;

public interface UserService {
    UserDTO registerUser(UserRegistrationRequest userRegistrationRequest);
    UserDTO getUserById(int userId);
    UserDTO updateUser(int userId, UserUpdateRequest userUpdateRequest);
    void deleteUserById(int id);
}

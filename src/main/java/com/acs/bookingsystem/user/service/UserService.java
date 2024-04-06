package com.acs.bookingsystem.user.service;

import com.acs.bookingsystem.user.dto.UserDTO;
import com.acs.bookingsystem.user.dto.UserRegistrationRequest;
import com.acs.bookingsystem.user.dto.UserUpdateRequest;

public interface UserService {
    UserDTO registerUser(UserRegistrationRequest userRegistrationRequest);
    UserDTO getUserById(int id);
    UserDTO updateUser(int userId, UserUpdateRequest userUpdateRequest);
    void deleteUserById(int id);
}

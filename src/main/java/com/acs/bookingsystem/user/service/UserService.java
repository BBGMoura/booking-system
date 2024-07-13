package com.acs.bookingsystem.user.service;

import com.acs.bookingsystem.authorization.entity.AuthUser;
import com.acs.bookingsystem.user.dto.UserDTO;
import com.acs.bookingsystem.user.request.UserRequest;
import com.acs.bookingsystem.user.request.UserUpdateRequest;

public interface UserService {
    UserDTO createUser(UserRequest userRequest);
    UserDTO getUserById(int userId);
    UserDTO getActiveUserById(int userId);
    UserDTO updateUser(int userId, UserUpdateRequest userUpdateRequest);
    void updateUserEmail(int userId, String updatedEmail);
    UserDTO saveAuthToUser(int userId, AuthUser authUser);
}

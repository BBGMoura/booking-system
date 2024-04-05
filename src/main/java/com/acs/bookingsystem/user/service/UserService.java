package com.acs.bookingsystem.user.service;

import com.acs.bookingsystem.user.dto.UserDTO;
import com.acs.bookingsystem.user.dto.UserRegistrationRequest;
import com.acs.bookingsystem.user.dto.UserUpdateRequest;

public interface UserService {
    public UserDTO registerUser(UserRegistrationRequest userRegistrationRequest);
    public UserDTO getUserById(int id);
    public UserDTO updateUser(UserUpdateRequest userUpdateRequest);
    public void deleteUserById(int id);
}

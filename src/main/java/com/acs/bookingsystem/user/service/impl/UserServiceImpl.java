package com.acs.bookingsystem.user.service.impl;

import com.acs.bookingsystem.user.dto.UserDTO;
import com.acs.bookingsystem.user.dto.UserRegistrationRequest;
import com.acs.bookingsystem.user.dto.UserUpdateRequest;
import com.acs.bookingsystem.user.entities.User;
import com.acs.bookingsystem.user.enums.Permission;
import com.acs.bookingsystem.user.exception.ErrorModel;
import com.acs.bookingsystem.user.exception.UserRequestException;
import com.acs.bookingsystem.user.mapper.UserMapper;
import com.acs.bookingsystem.user.repository.UserRepository;
import com.acs.bookingsystem.user.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;
    private UserMapper userMapper;

    public UserDTO registerUser(UserRegistrationRequest userRegistrationRequest) {
        validateEmail(userRegistrationRequest.getEmail());
        final User savedUser = userRepository.save(new User(userRegistrationRequest.getFirstName(),
                                                            userRegistrationRequest.getLastName(),
                                                            userRegistrationRequest.getEmail(),
                                                            userRegistrationRequest.getPhoneNumber(),
                                                            Permission.USER));
        return userMapper.mapUserToDTO(savedUser);
    }

    public UserDTO getUserById(int id){
        return userMapper.mapUserToDTO(findUserById(id));
    }

    public UserDTO updateUser(int userId, UserUpdateRequest userUpdateRequest){
        User userToUpdate = findUserById(userId);

        if (userUpdateRequest.getEmail() != null) {
            validateEmail(userUpdateRequest.getEmail());
            userToUpdate.setEmail(userUpdateRequest.getEmail());
        }
        if (userUpdateRequest.getFirstName() != null) {
            userToUpdate.setFirstName(userUpdateRequest.getFirstName());
        }
        if (userUpdateRequest.getLastName() != null) {
            userToUpdate.setLastName(userUpdateRequest.getLastName());
        }
        if (userUpdateRequest.getPhoneNumber() != null) {
            userToUpdate.setPhoneNumber(userUpdateRequest.getPhoneNumber());
        }

        User updatedUser = userRepository.save(userToUpdate);

        return userMapper.mapUserToDTO(updatedUser);
    }

    public void deleteUserById(int id){
        userRepository.delete(findUserById(id));
    }

    private void validateEmail(String email) {
        userRepository.findByEmail(email)
                      .ifPresent(user -> {
                          throw new UserRequestException(createError("EMAIL_ALREADY_EXISTS", String.format("User with email %s already exists", email)));
                      });
    }

    private User findUserById(int id){
        return userRepository.findById(id)
                             .orElseThrow(() -> new UserRequestException(createError("INVALID_ID", String.format("Could not find user with ID %d", id))));
    }

    private List<ErrorModel> createError(String code, String message) {
        List<ErrorModel> errorModelList = new ArrayList<>();
        errorModelList.add(new ErrorModel(code, message));
        return errorModelList;
    }
}

package com.acs.bookingsystem.user.service.impl;

import com.acs.bookingsystem.common.exception.NotFoundException;
import com.acs.bookingsystem.user.dto.UserDTO;
import com.acs.bookingsystem.user.request.UserRegistrationRequest;
import com.acs.bookingsystem.user.request.UserUpdateRequest;
import com.acs.bookingsystem.user.entities.User;
import com.acs.bookingsystem.user.enums.Permission;
import com.acs.bookingsystem.common.exception.model.ErrorCode;
import com.acs.bookingsystem.common.exception.RequestException;
import com.acs.bookingsystem.user.mapper.UserMapper;
import com.acs.bookingsystem.user.repository.UserRepository;
import com.acs.bookingsystem.user.service.UserService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);
    private UserRepository userRepository;
    private UserMapper userMapper;

    public UserDTO registerUser(UserRegistrationRequest userRegistrationRequest) {
        validateEmail(userRegistrationRequest.getEmail());
        final User savedUser = userRepository.save(new User(userRegistrationRequest.getFirstName(),
                                                            userRegistrationRequest.getLastName(),
                                                            userRegistrationRequest.getEmail(),
                                                            userRegistrationRequest.getPhoneNumber(),
                                                            true,
                                                            Permission.USER));
        return userMapper.mapUserToDTO(savedUser);
    }

    public UserDTO getUserById(int id) {
        return userMapper.mapUserToDTO(findUserById(id));
    }

    public UserDTO getActiveUserById(int id) {
        User user = findUserById(id);
        if (!user.isActive()) {
            LOG.debug("User with ID: {} is not active.", id);
            throw new RequestException("User is not active.", ErrorCode.INACTIVE_USER);
        }
        return userMapper.mapUserToDTO(user);
    }

    public UserDTO updateUser(int userId, UserUpdateRequest userUpdateRequest) {
        LOG.debug("Updating user with request: {}", userUpdateRequest);
        User userToUpdate = findUserById(userId);

        if (userUpdateRequest.getEmail() != null && !userUpdateRequest.getEmail().trim().isEmpty()) {
            validateEmail(userUpdateRequest.getEmail());
            userToUpdate.setEmail(userUpdateRequest.getEmail());
        }
        if (userUpdateRequest.getFirstName() != null && !userUpdateRequest.getFirstName().trim().isEmpty()) {
            userToUpdate.setFirstName(userUpdateRequest.getFirstName());
        }
        if (userUpdateRequest.getLastName() != null && !userUpdateRequest.getLastName().trim().isEmpty()) {
            userToUpdate.setLastName(userUpdateRequest.getLastName());
        }
        if (userUpdateRequest.getPhoneNumber() != null && !userUpdateRequest.getPhoneNumber().trim().isEmpty()) {
            userToUpdate.setPhoneNumber(userUpdateRequest.getPhoneNumber());
        }

        User updatedUser = userRepository.save(userToUpdate);

        return userMapper.mapUserToDTO(updatedUser);
    }

    public void deactivateUserById(int id){
        User user = findUserById(id);
        user.setActive(false);
        userRepository.save(user);
    }

    private void validateEmail(String email) {
        userRepository.findByEmail(email)
                      .ifPresent(user -> {
                          throw new RequestException("Email "+email+" is already in use", ErrorCode.EMAIL_ALREADY_EXISTS);
                      });
    }

    private User findUserById(int id){
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            LOG.debug("User with ID: {} is not found.", id);
            throw new NotFoundException("Could not find user.", ErrorCode.INVALID_USER_ID);
        }
        return userOptional.get();
    }
}

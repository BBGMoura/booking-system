package com.acs.bookingsystem.user.service.impl;

import com.acs.bookingsystem.authorization.entity.AuthUser;
import com.acs.bookingsystem.common.exception.NotFoundException;
import com.acs.bookingsystem.user.dto.UserDTO;
import com.acs.bookingsystem.user.request.UserRequest;
import com.acs.bookingsystem.user.request.UserUpdateRequest;
import com.acs.bookingsystem.user.entities.User;
import com.acs.bookingsystem.common.exception.model.ErrorCode;
import com.acs.bookingsystem.common.exception.RequestException;
import com.acs.bookingsystem.user.mapper.UserMapper;
import com.acs.bookingsystem.user.repository.UserRepository;
import com.acs.bookingsystem.user.service.UserService;
import jakarta.validation.Valid;
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

    public UserDTO createUser(@Valid UserRequest userRequest) {
        validateEmail(userRequest.getEmail());
        final User savedUser = userRepository.save(User.builder()
                                                       .firstName(userRequest.getFirstName())
                                                       .lastName(userRequest.getLastName())
                                                       .email(userRequest.getEmail())
                                                       .phoneNumber(userRequest.getPhoneNumber())
                                                       .build());
        return userMapper.mapUserToDTO(savedUser);
    }

    public UserDTO getUserById(int userId) {
        return userMapper.mapUserToDTO(findUserById(userId));
    }

    public UserDTO getActiveUserById(int userId) {
        User user = findUserById(userId);
//        if (!user.isActive()) {
//            LOG.debug("User with ID: {} is not active.", id);
//            throw new RequestException("User is not active.", ErrorCode.INACTIVE_USER);
//        }
        return userMapper.mapUserToDTO(user);
    }

    public UserDTO updateUser(int userId, UserUpdateRequest userUpdateRequest) {
        LOG.debug("Updating user with request: {}", userUpdateRequest);
        User userToUpdate = findUserById(userId);

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

    @Override
    public void updateUserEmail(int userId, String updatedEmail) {
        userRepository.findByEmail(updatedEmail)
                      .ifPresent(user -> {
                          throw new RequestException("Email "+updatedEmail+" is already in use", ErrorCode.EMAIL_ALREADY_EXISTS);
                      });

        User user = findUserById(userId);
        user.setEmail(updatedEmail);
        userRepository.save(user);
    }

    public UserDTO saveAuthToUser(int userId, AuthUser authUser) {
        final User user = findUserById(userId);
        user.setAuthUser(authUser);
        userRepository.save(user);
        return userMapper.mapUserToDTO(user);
    }

    private User findUserById(int userId){
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            LOG.debug("User with ID: {} is not found.", userId);
            throw new NotFoundException("Could not find user.", ErrorCode.INVALID_USER_ID);
        }
        return userOptional.get();
    }
}

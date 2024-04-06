package com.acs.bookingsystem.user.service.impl;

import com.acs.bookingsystem.user.dto.UserDTO;
import com.acs.bookingsystem.user.dto.UserRegistrationRequest;
import com.acs.bookingsystem.user.dto.UserUpdateRequest;
import com.acs.bookingsystem.user.entities.User;
import com.acs.bookingsystem.user.enums.Permission;
import com.acs.bookingsystem.user.exception.UserRequestException;
import com.acs.bookingsystem.user.mapper.UserMapper;
import com.acs.bookingsystem.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    UserServiceImpl userService;

    @Test
    void registerUser() {
        // given
        UserRegistrationRequest request = createUserRegRequest();
        User user = createUser();
        UserDTO userDTO = createUserDTO();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.mapUserToDTO(user)).thenReturn(userDTO);

        //when
        UserDTO savedUserDto = userService.registerUser(request);

        //then
        verify(userRepository, times(1)).findByEmail(request.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).mapUserToDTO(user);
        assertNotNull(savedUserDto);
        assertEquals(savedUserDto.email(), user.getEmail());
    }

    @Test
    void doNotRegisterUserDuplicateEmail() {
        //given
        UserRegistrationRequest request = createUserRegRequest();
        User user = createUser();

        //when
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        //then
        assertThrows(UserRequestException.class, () -> userService.registerUser(request));
        verify(userRepository, times(1)).findByEmail(request.getEmail());
    }

    @Test
    void getUserByIdDoesntExist() {
        //given
        int userId = 1;

        //when
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        //then
        assertThrows(UserRequestException.class, () ->  userService.getUserById(userId));
    }

    @Test
    void getByUserId() {
        //given
        User user = createUser();
        int userId = 1;
        UserDTO userDTO = createUserDTO();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.mapUserToDTO(user)).thenReturn(userDTO);

        //when
        UserDTO retrievedUser = userService.getUserById(userId);
        assertEquals(userDTO, retrievedUser);
    }

    @Test
    void updateUser() {
        //given
        int userId = 1;
        UserUpdateRequest request = createUserUpdateRequest();
        User user = createUser();
        UserDTO userDTO = createUserDTO();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.mapUserToDTO(user)).thenReturn(userDTO);

        //when
        UserDTO savedUser = userService.updateUser(userId, request);

        //then
        assertEquals(userId, savedUser.id());
        assertEquals(request.getFirstName(), savedUser.firstName());
    }

    @Test
    void deleteUserById() {
        //given
        int userId = 1;
        User user = createUser();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        //when
        userService.deleteUserById(userId);

        //then
        verify(userRepository).delete(user);
    }

    private UserRegistrationRequest createUserRegRequest() {
        return new UserRegistrationRequest("John",
                                           "Doe",
                                           "john@example.com",
                                           "1234567890");
    }

    private UserUpdateRequest createUserUpdateRequest() {
        return new UserUpdateRequest("John",
                                     "Doe",
                                     "john@example.com",
                                     "1234567890");
    }

    private User createUser() {
        return new User("John",
                        "Doe",
                        "john@example.com",
                        "1234567890",
                        Permission.USER);
    }

    private UserDTO createUserDTO() {
        return new UserDTO(1,
                           "John",
                           "Doe",
                           "john@example.com",
                           "1234567890",
                           Permission.USER);
    }
}

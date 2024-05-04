package com.acs.bookingsystem.user.controller;

import com.acs.bookingsystem.user.dto.UserDTO;
import com.acs.bookingsystem.user.request.UserRegistrationRequest;
import com.acs.bookingsystem.user.request.UserUpdateRequest;
import com.acs.bookingsystem.user.enums.Permission;
import com.acs.bookingsystem.user.service.impl.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import static com.acs.bookingsystem.TestDataUtil.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @MockBean
    UserServiceImpl userService;
    @Autowired
    MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerUser_ShouldReturnCreatedUser() throws Exception {
        UserRegistrationRequest request = createUserRegistrationRequest();
        UserDTO responseDto = createUserDTO();

        given(userService.registerUser(any(UserRegistrationRequest.class))).willReturn(responseDto);

        mockMvc.perform(post("/user/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isCreated())
               .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
    }

    @Test
    void getUserById_ShouldReturnUser() throws Exception {
        UserDTO responseDto = createUserDTO();

        given(userService.getUserById(1)).willReturn(responseDto);

        mockMvc.perform(get("/user/{id}", 1))
               .andExpect(status().isOk())
               .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() throws Exception {
        UserUpdateRequest request = createUserUpdateRequest();
        UserDTO responseDto = createUserDTO();  // Assuming ID does not change

        given(userService.updateUser(eq(1), any(UserUpdateRequest.class))).willReturn(responseDto);

        mockMvc.perform(put("/user/update/{id}", 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk())
               .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
    }

    @Test
    void deactivateUser_ShouldReturnNoContent() throws Exception {
        doNothing().when(userService).deactivateUserById(1);

        mockMvc.perform(patch("/user/deactivate/{id}", 1))
               .andExpect(status().isNoContent());
    }
}
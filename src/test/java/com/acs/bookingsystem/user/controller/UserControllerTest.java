package com.acs.bookingsystem.user.controller;

import com.acs.bookingsystem.user.dto.UserDTO;
import com.acs.bookingsystem.user.enums.Permission;
import com.acs.bookingsystem.user.exception.UserError;
import com.acs.bookingsystem.user.exception.UserRequestException;
import com.acs.bookingsystem.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;


import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @MockBean
    UserServiceImpl userService;

    @Autowired
    MockMvc mockMvc;

    @Test
    void testFindByID() throws Exception {
        UserDTO userDTO = new UserDTO(1,
                                      "Joe",
                                      "Malone",
                                      "joem@gmail.com",
                                      "01234567891",
                                      Permission.USER);
        int userId = 1;

        when(userService.getUserById(userId)).thenReturn(userDTO);

        mockMvc.perform(get("/user/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(1))
               .andExpect(jsonPath("$.firstName").value("Joe"));
    }

    @Test
    void testDoesntFindByID() throws Exception {
        when(userService.getUserById(anyInt())).thenThrow(new UserRequestException("Could not find user with ID", UserError.INVALID_ID));

        mockMvc.perform(get("/user/123")
                                              .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.error").value("ID is Invalid"))
               .andExpect(jsonPath("$.message").value("Could not find user with ID"));
    }
}
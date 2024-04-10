package com.acs.bookingsystem.user.controller;

import com.acs.bookingsystem.user.dto.UserDTO;
import com.acs.bookingsystem.user.enums.Permission;
import com.acs.bookingsystem.common.exception.ErrorCode;
import com.acs.bookingsystem.common.exception.RequestException;
import com.acs.bookingsystem.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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
        when(userService.getUserById(anyInt())).thenThrow(new RequestException("Could not find user with ID", ErrorCode.INVALID_ID));

        mockMvc.perform(get("/user/123")
                                              .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.error").value("ID is Invalid"))
               .andExpect(jsonPath("$.message").value("Could not find user with ID"));
    }
}
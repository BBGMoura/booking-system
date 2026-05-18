package com.acs.bookingsystem.user.controller;

import com.acs.bookingsystem.security.config.SecurityConfig;
import com.acs.bookingsystem.security.util.JwtUtil;
import com.acs.bookingsystem.user.UserTestData;
import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.model.UserProfile;
import com.acs.bookingsystem.user.request.UpdateUserInfoRequest;
import com.acs.bookingsystem.user.request.UpdateUserRequest;
import com.acs.bookingsystem.user.response.AuthenticateResponse;
import com.acs.bookingsystem.user.response.UserStatusResponse;
import com.acs.bookingsystem.user.service.AuthenticationService;
import com.acs.bookingsystem.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private UserService userService;
    @MockitoBean private AuthenticationService authenticationService;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private AuthenticationProvider authenticationProvider;

    private final User user = UserTestData.user;
    private final UserProfile profile = UserTestData.adminUserProfile;

    @BeforeEach
    void setup() {
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void givenAuthenticatedUser_whenGetProfile_thenReturns200() throws Exception {
        when(userService.getUserProfile(any(int.class))).thenReturn(profile);

        mockMvc.perform(get("/api/v1/user"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.uid").value(profile.uid().toString()))
               .andExpect(jsonPath("$.email").value(profile.email()))
               .andExpect(jsonPath("$.firstName").value(profile.firstName()));
    }

    @Test
    void givenValidRequest_whenUpdateUserInfo_thenReturns200() throws Exception {
        UpdateUserInfoRequest request = new UpdateUserInfoRequest("New", "Name", null);
        when(userService.updateUserInfo(any(int.class), any())).thenReturn(profile);

        mockMvc.perform(patch("/api/v1/user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk());

        verify(userService).updateUserInfo(any(int.class), eq(request));
    }

    @Test
    void givenValidCredentials_whenUpdateCredentials_thenReturns200WithToken() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("new@example.com", "NewPass1!");
        AuthenticateResponse response = AuthenticateResponse.builder().token("new-jwt-token").build();
        when(authenticationService.updateUserCredentials(any(int.class), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/user/credentials")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.token").value("new-jwt-token"));
    }

    @Test
    void givenAuthenticatedUser_whenDisableUser_thenReturns200() throws Exception {
        UserStatusResponse response = UserStatusResponse.builder()
                .uid(profile.uid())
                .enabled(false)
                .build();
        when(userService.updateEnableStatus(any(int.class), eq(false))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/user/status"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    void givenUnauthenticated_whenGetProfile_thenReturns403() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/v1/user"))
               .andExpect(status().isForbidden());
    }
}

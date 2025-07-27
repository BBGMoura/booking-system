package com.acs.bookingsystem.user.controller;

import com.acs.bookingsystem.common.config.JsonConfig;
import com.acs.bookingsystem.security.config.SecurityConfig;
import com.acs.bookingsystem.security.util.JwtUtil;
import com.acs.bookingsystem.user.UserTestData;
import com.acs.bookingsystem.user.model.UserProfile;
import com.acs.bookingsystem.user.request.InviteRequest;
import com.acs.bookingsystem.user.response.InviteResponse;
import com.acs.bookingsystem.user.service.AuthenticateService;
import com.acs.bookingsystem.user.service.UserProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserAdminController.class)
@Import({SecurityConfig.class, JsonConfig.class})
class UserAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private AuthenticateService authenticateService;

    @MockitoBean
    private UserProfileService userProfileService;

    @MockitoBean
    private AuthenticationProvider authenticationProvider;

    private final UserProfile adminUserProfile = UserTestData.adminUserProfile;
    private final UserProfile userProfile = UserTestData.userProfile;
    private final Page<UserProfile> page = UserTestData.userPage;
    private final InviteRequest inviteRequest = UserTestData.inviteRequest;
    private final InviteResponse inviteResponse = UserTestData.inviteResponse;

    @Test
    void givenUserAuthority_whenGetUser_thenReturnForbidden() throws Exception {
        mockMvc.perform(post("/admin/user", 1))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Admin can successfully create a dance class")
    void givenAdminAuthority_whenGetUser_thenReturnOk() throws Exception {
        when(userProfileService.getUserProfile(1)).thenReturn(adminUserProfile);

        mockMvc.perform(get("/admin/user/{userId}", adminUserProfile.userId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(adminUserProfile.userId()))
                .andExpect(jsonPath("$.firstName").value(adminUserProfile.firstName()))
                .andExpect(jsonPath("$.lastName").value(adminUserProfile.lastName()))
                .andExpect(jsonPath("$.email").value(adminUserProfile.email()))
                .andExpect(jsonPath("$.phoneNumber").value(adminUserProfile.phoneNumber()))
                .andExpect(jsonPath("$.enabled").value(adminUserProfile.enabled()))
                .andExpect(jsonPath("$.role").value(adminUserProfile.role().toString()));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void givenAdminAuthority_whenGetUsers_thenReturnOk() throws Exception {
        when(userProfileService.getUserProfiles(0, 5)).thenReturn(page);

        mockMvc.perform(get("/admin/users?page=0&size=5")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].email").value(adminUserProfile.email()))
                .andExpect(jsonPath("$.content[1].email").value(userProfile.email()))
                .andExpect(jsonPath("$.page.totalElements").value(page.getTotalElements()))
                .andExpect(jsonPath("$.page.size").value(page.getSize()));

        verify(userProfileService).getUserProfiles(0, 5);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void givenAdminAuthority_inviteUser_shouldReturnOk() throws Exception {
        when(authenticateService.invite(inviteRequest)).thenReturn(inviteResponse);

        mockMvc.perform(post("/admin/user/invite").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(inviteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.email").value(inviteResponse.email()))
                .andExpect(jsonPath(("$.role")).value(inviteResponse.role().toString()));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void givenAdminAuthority_whenUpdateUserStatus_shouldReturnNoContent() throws Exception {

        mockMvc.perform(patch("/admin/user/{userId}/status", 1)
                                .param("enable", Boolean.TRUE.toString()))
                .andExpect(status().isNoContent());
    }

}
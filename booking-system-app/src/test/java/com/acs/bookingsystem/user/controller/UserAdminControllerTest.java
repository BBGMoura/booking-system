package com.acs.bookingsystem.user.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acs.bookingsystem.common.config.JsonConfig;
import com.acs.bookingsystem.security.config.SecurityConfig;
import com.acs.bookingsystem.security.util.JwtUtil;
import com.acs.bookingsystem.user.UserTestData;
import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.model.UserProfile;
import com.acs.bookingsystem.user.request.InviteRequest;
import com.acs.bookingsystem.user.request.UpdateUserStatusRequest;
import com.acs.bookingsystem.user.response.InviteResponse;
import com.acs.bookingsystem.user.response.UserStatusResponse;
import com.acs.bookingsystem.user.service.AuthenticationService;
import com.acs.bookingsystem.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserAdminController.class)
@Import({SecurityConfig.class, JsonConfig.class})
class UserAdminControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private JwtUtil jwtUtil;

  @MockitoBean private UserService userService;

  @MockitoBean private AuthenticationService authenticationService;

  @MockitoBean private AuthenticationProvider authenticationProvider;

  private final User adminUser = UserTestData.adminUser;
  private final UserProfile adminUserProfile = UserTestData.adminUserProfile;
  private final UserProfile userProfile = UserTestData.userProfile;
  private final Page<UserProfile> page = UserTestData.userPage;
  private final InviteRequest inviteRequest = UserTestData.inviteRequest;
  private final InviteResponse inviteResponse = UserTestData.inviteResponse;

  @AfterEach
  void teardown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void givenUserAuthority_whenPostToAdmin_thenReturnForbidden() throws Exception {
    mockMvc.perform(post("/api/v1/admin/users/invite")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = {"ADMIN"})
  @DisplayName("Admin can successfully get a user profile")
  void givenAdminAuthority_whenGetUser_thenReturnOk() throws Exception {
    when(userService.getUserProfileByUid(adminUserProfile.uid())).thenReturn(adminUserProfile);

    mockMvc
        .perform(get("/api/v1/admin/users/{userUid}", adminUserProfile.uid()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.uid").value(adminUserProfile.uid().toString()))
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
    when(userService.getUserProfiles(0, 5)).thenReturn(page);

    mockMvc
        .perform(get("/api/v1/admin/users?page=0&size=5").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content[0].email").value(adminUserProfile.email()))
        .andExpect(jsonPath("$.content[1].email").value(userProfile.email()))
        .andExpect(jsonPath("$.page.totalElements").value(page.getTotalElements()))
        .andExpect(jsonPath("$.page.size").value(page.getSize()));

    verify(userService).getUserProfiles(0, 5);
  }

  @Test
  @WithMockUser(roles = {"ADMIN"})
  void givenAdminAuthority_inviteUser_shouldReturnOk() throws Exception {
    when(userService.invite(inviteRequest)).thenReturn(inviteResponse);

    mockMvc
        .perform(
            post("/api/v1/admin/users/invite")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inviteRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.uid").exists())
        .andExpect(jsonPath("$.email").value(inviteResponse.email()))
        .andExpect(jsonPath("$.role").value(inviteResponse.role().toString()));
  }

  @Test
  @WithMockUser(roles = {"ADMIN"})
  void givenAdminAuthority_whenEnableUser_thenCallsEnableUser() throws Exception {
    UserStatusResponse response =
        UserStatusResponse.builder().uid(UserTestData.ADMIN_UUID).enabled(true).build();
    when(userService.enableUser(UserTestData.ADMIN_UUID)).thenReturn(response);

    mockMvc
        .perform(
            patch("/api/v1/admin/users/{userUid}", UserTestData.ADMIN_UUID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateUserStatusRequest(true))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.enabled").value(true));

    verify(userService).enableUser(UserTestData.ADMIN_UUID);
    verify(userService, never()).disableUser(any(UUID.class), any(User.class));
  }

  @Test
  void givenAdminAuthority_whenDisableUser_thenCallsDisableUser() throws Exception {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(adminUser, null, adminUser.getAuthorities()));

    UserStatusResponse response =
        UserStatusResponse.builder().uid(UserTestData.ADMIN_UUID).enabled(false).build();
    when(userService.disableUser(UserTestData.ADMIN_UUID, adminUser)).thenReturn(response);

    mockMvc
        .perform(
            patch("/api/v1/admin/users/{userUid}", UserTestData.ADMIN_UUID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateUserStatusRequest(false))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.enabled").value(false));

    verify(userService).disableUser(UserTestData.ADMIN_UUID, adminUser);
    verify(userService, never()).enableUser(UserTestData.ADMIN_UUID);
  }

  @Test
  void givenNullEnabled_whenUpdateUserStatus_thenReturn400() throws Exception {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(adminUser, null, adminUser.getAuthorities()));

    mockMvc
        .perform(
            patch("/api/v1/admin/users/{userUid}", UserTestData.ADMIN_UUID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"enabled\":null}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.details[0].field").value("enabled"));
  }
}

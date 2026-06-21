package com.acs.bookingsystem.danceclass.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acs.bookingsystem.danceclass.DanceClassTestData;
import com.acs.bookingsystem.danceclass.dto.DanceClassDTO;
import com.acs.bookingsystem.danceclass.entity.DanceClass;
import com.acs.bookingsystem.danceclass.mapper.DanceClassMapper;
import com.acs.bookingsystem.danceclass.service.DanceClassService;
import com.acs.bookingsystem.security.config.SecurityConfig;
import com.acs.bookingsystem.security.util.JwtUtil;
import com.acs.bookingsystem.user.enums.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DanceClassAdminController.class)
@Import({SecurityConfig.class})
class DanceClassAdminControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private JwtUtil jwtUtil;

  @MockitoBean private DanceClassMapper mapper;

  @MockitoBean private DanceClassService danceClassService;

  @MockitoBean private AuthenticationProvider authenticationProvider;

  private final DanceClass danceClass = DanceClassTestData.danceClass;
  private final DanceClassDTO danceClassDTO = DanceClassTestData.danceClassDTO;

  @Test
  void givenUserAuthority_whenCreateDanceClass_thenReturnForbidden() throws Exception {
    mockMvc.perform(post("/api/v1/admin/dance-classes")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = {"ADMIN"})
  void givenAdminCreateDanceClass_thenReturnCreated() throws Exception {
    when(danceClassService.createDanceClass(any())).thenReturn(danceClass);
    when(mapper.map(any())).thenReturn(danceClassDTO);

    mockMvc
        .perform(
            post("/api/v1/admin/dance-classes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DanceClassTestData.danceClassRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.classType", equalTo("PRIVATE")))
        .andExpect(jsonPath("$.active", equalTo(Boolean.TRUE)))
        .andExpect(jsonPath("$.pricePerHour", equalTo(0)))
        .andExpect(jsonPath("$.role", equalTo(Role.ROLE_USER.toString())));
  }

  @Test
  @WithMockUser(roles = {"ADMIN"})
  void givenAdminGetAllClassTypes_thenReturnOk() throws Exception {
    mockMvc
        .perform(get("/api/v1/admin/dance-classes/class-types"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(
            jsonPath("$")
                .value(containsInAnyOrder("PRACTICE", "PRIVATE", "GROUP", "UNAVAILABLE", "OTHER")));
  }

  @Test
  @WithMockUser(roles = {"ADMIN"})
  void givenAdminDeactivateDanceClass_thenReturnNoContent() throws Exception {
    mockMvc
        .perform(delete("/api/v1/admin/dance-classes/{classType}", "PRIVATE"))
        .andExpect(status().isNoContent());
  }
}

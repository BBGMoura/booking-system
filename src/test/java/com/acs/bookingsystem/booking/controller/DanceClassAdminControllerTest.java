package com.acs.bookingsystem.booking.controller;

import com.acs.bookingsystem.booking.DanceClassTestData;
import com.acs.bookingsystem.booking.service.DanceClassService;
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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DanceClassAdminController.class)
@Import({SecurityConfig.class})
class DanceClassAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private DanceClassService danceClassService;

    @MockitoBean
    private AuthenticationProvider authenticationProvider;

    @Test
    void givenUserHasUserAuthority_whenCreateDanceClass_thenReturnForbidden() throws Exception {
        mockMvc.perform(post("/admin/dance-class"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void givenAdminCreateDanceClass_thenReturnCreated() throws Exception {
        when(danceClassService.createDanceClass(any())).thenReturn(DanceClassTestData.danceClassDTO);

        mockMvc.perform(post("/admin/dance-class").contentType(MediaType.APPLICATION_JSON)
                                                             .content(objectMapper.writeValueAsString(DanceClassTestData.danceClassRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", equalTo(1)))
                .andExpect(jsonPath("$.classType", equalTo("PRIVATE")))
                .andExpect(jsonPath("$.active", equalTo(Boolean.TRUE)))
                .andExpect(jsonPath("$.pricePerHour", equalTo(0)))
                .andExpect(jsonPath("$.role", equalTo(Role.ROLE_USER.toString())));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void givenAdminGetAllClassTypes_thenReturnOkay() throws Exception {
        mockMvc.perform(get("/admin/class-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").value(containsInAnyOrder("PRACTICE", "PRIVATE", "GROUP", "UNAVAILABLE", "OTHER")));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void givenAdminDeactivateDanceClassType_thenReturnNoContent() throws Exception {
        mockMvc.perform(patch("/admin/dance-classes/{classType}/deactivate", "PRIVATE"))
                .andExpect(status().isNoContent());
    }
}
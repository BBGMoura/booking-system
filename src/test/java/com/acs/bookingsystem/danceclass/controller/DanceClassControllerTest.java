package com.acs.bookingsystem.danceclass.controller;

import com.acs.bookingsystem.danceclass.DanceClassTestData;
import com.acs.bookingsystem.danceclass.dto.DanceClassDTO;
import com.acs.bookingsystem.danceclass.entity.DanceClass;
import com.acs.bookingsystem.danceclass.enums.ClassType;
import com.acs.bookingsystem.danceclass.mapper.DanceClassMapper;
import com.acs.bookingsystem.danceclass.service.impl.DanceClassService;
import com.acs.bookingsystem.security.config.SecurityConfig;
import com.acs.bookingsystem.security.util.JwtUtil;
import com.acs.bookingsystem.user.UserTestData;
import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(authorities = {"USER"})
@WebMvcTest(DanceClassController.class)
@Import({SecurityConfig.class})
class DanceClassControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private DanceClassMapper mapper;

    @MockitoBean
    private DanceClassService danceClassService;

    @MockitoBean
    private AuthenticationProvider authenticationProvider;

    private final List<ClassType> classTypes = DanceClassTestData.classTypes;
    private final DanceClass danceClass = DanceClassTestData.danceClass;
    private final DanceClassDTO danceClassDTO = DanceClassTestData.danceClassDTO;
    private final User user = UserTestData.user;

    @BeforeEach
    void setup() {
        Authentication auth = new UsernamePasswordAuthenticationToken(user,
                                                                      null,
                                                                      user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void givenGetAllActiveDanceClassTypes_thenReturnOk() throws Exception {
        when(danceClassService.getAllActiveDanceClassTypes(any())).thenReturn(classTypes);

        mockMvc.perform(get("/dance-classes/class-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").value(containsInAnyOrder("PRACTICE", "PRIVATE", "GROUP")));
    }

    @Test
    void givenGetDanceClassByClassType_thenReturnOk() throws Exception {
        when(danceClassService.getActiveDanceClass(any(), any())).thenReturn(danceClass);
        when(mapper.map(any())).thenReturn(danceClassDTO);

        mockMvc.perform(get("/dance-classes").param("classType", "PRIVATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(danceClassDTO.id()))
                .andExpect(jsonPath("$.classType").value(danceClassDTO.classType().toString()))
                .andExpect(jsonPath("$.active").value(danceClassDTO.active()))
                .andExpect(jsonPath("$.pricePerHour").value(danceClassDTO.pricePerHour()))
                .andExpect(jsonPath("$.role", equalTo(Role.ROLE_USER.toString())));
    }
}
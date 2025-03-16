package com.acs.bookingsystem.booking.controller;

import com.acs.bookingsystem.booking.DanceClassTestData;
import com.acs.bookingsystem.booking.dto.DanceClassDTO;
import com.acs.bookingsystem.booking.enums.ClassType;
import com.acs.bookingsystem.booking.service.DanceClassService;
import com.acs.bookingsystem.common.security.config.SecurityConfig;
import com.acs.bookingsystem.common.security.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(authorities = {"USER"})
@WebMvcTest(DanceClassController.class)
@ImportAutoConfiguration(SecurityConfig.class)
class DanceClassControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DanceClassService danceClassService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private AuthenticationProvider authenticationProvider;

    private final List<ClassType> classTypes = DanceClassTestData.classTypes;
    private final DanceClassDTO danceClass = DanceClassTestData.danceClass;

    @Test
    public void givenGetAllActiveDanceClassTypes_thenReturnOk() throws Exception {
        when(danceClassService.getAllActiveDanceClassTypes()).thenReturn(classTypes);

        mockMvc.perform(get("/dance-classes/class-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").value(containsInAnyOrder("PRACTICE", "PRIVATE", "GROUP")));
    }

    @Test
    public void givenGetDanceClassByClassType_thenReturnOk() throws Exception {
        when(danceClassService.getActiveDanceClassByClassType(ClassType.PRIVATE)).thenReturn(danceClass);

        mockMvc.perform(get("/dance-classes").param("classType", "PRIVATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(danceClass.id()))
                .andExpect(jsonPath("$.classType").value(danceClass.classType().toString()))
                .andExpect(jsonPath("$.active").value(danceClass.active()))
                .andExpect(jsonPath("$.pricePerHour").value(danceClass.pricePerHour()));
    }
}
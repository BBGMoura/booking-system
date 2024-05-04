package com.acs.bookingsystem.booking.controller;

import com.acs.bookingsystem.TestDataUtil;
import com.acs.bookingsystem.booking.dto.DanceClassDTO;
import com.acs.bookingsystem.booking.enums.ClassType;
import com.acs.bookingsystem.booking.request.DanceClassRequest;
import com.acs.bookingsystem.booking.service.DanceClassService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.acs.bookingsystem.TestDataUtil.createDanceClassDTO;
import static com.acs.bookingsystem.TestDataUtil.createDanceClassRequest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DanceClassController.class)
class DanceClassControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DanceClassService danceClassService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createDanceClass_ShouldReturnCreatedDanceClass() throws Exception {
        DanceClassRequest request = createDanceClassRequest(ClassType.PRACTICE);
        DanceClassDTO responseDto = createDanceClassDTO(ClassType.PRACTICE, true);

        given(danceClassService.createDanceClass(any(DanceClassRequest.class))).willReturn(responseDto);

        mockMvc.perform(post("/dance-class")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isCreated())
               .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
    }

    @Test
    void getAllActiveDanceClassTypes_ShouldReturnClassTypesList() throws Exception {
        List<ClassType> classTypes = List.of(ClassType.PRIVATE, ClassType.PRACTICE);

        given(danceClassService.getAllActiveDanceClassTypes()).willReturn(classTypes);

        mockMvc.perform(get("/dance-class/class-types"))
               .andExpect(status().isOk())
               .andExpect(content().json(objectMapper.writeValueAsString(classTypes)));
    }

    @Test
    void getDanceClassByActiveClassType_ShouldReturnDanceClass() throws Exception {
        DanceClassDTO responseDto = createDanceClassDTO(ClassType.PRIVATE, true);

        given(danceClassService.getDanceClassByActiveClassType(ClassType.PRIVATE)).willReturn(responseDto);

        mockMvc.perform(get("/dance-class/{classType}", ClassType.PRIVATE))
               .andExpect(status().isOk())
               .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
    }

    @Test
    void deactivateDanceClassType_ShouldReturnNoContent() throws Exception {
        doNothing().when(danceClassService).deactivateDanceClass(ClassType.PRIVATE);

        mockMvc.perform(delete("/dance-class/deactivate/{classType}", ClassType.PRIVATE))
               .andExpect(status().isNoContent());
    }
}
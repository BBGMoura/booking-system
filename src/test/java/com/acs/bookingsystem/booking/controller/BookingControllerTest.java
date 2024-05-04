package com.acs.bookingsystem.booking.controller;

import com.acs.bookingsystem.booking.dto.BookingDTO;
import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.booking.request.BookingRequest;
import com.acs.bookingsystem.booking.service.impl.BookingServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.acs.bookingsystem.TestDataUtil.createBookingDTO;
import static com.acs.bookingsystem.TestDataUtil.createBookingRequest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingServiceImpl bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createBooking_ShouldReturnCreatedBooking() throws Exception {
        BookingRequest request = createBookingRequest();
        BookingDTO responseDto = createBookingDTO();

        given(bookingService.createBooking(any(BookingRequest.class))).willReturn(responseDto);

        mockMvc.perform(post("/booking")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isCreated())
               .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
    }

    @Test
    void getBookingByBookingId_ShouldReturnBooking() throws Exception {
        BookingDTO responseDto = createBookingDTO();

        given(bookingService.getBookingById(1)).willReturn(responseDto);

        mockMvc.perform(get("/booking/{id}", 1))
               .andExpect(status().isOk())
               .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
    }

    @Test
    void getBookingsByUserId_ShouldReturnBookingsList() throws Exception {
        BookingDTO responseDto = createBookingDTO();
        List<BookingDTO> bookings = Arrays.asList(responseDto, responseDto);

        given(bookingService.getAllBookingsByUser(1)).willReturn(bookings);

        mockMvc.perform(get("/booking/user/{userId}", 1))
               .andExpect(status().isOk())
               .andExpect(content().json(objectMapper.writeValueAsString(bookings)));
    }

    @Test
    void getBookingsByRoomAndTime_ShouldReturnBookingsList() throws Exception {
        BookingDTO responseDto = createBookingDTO();
        List<BookingDTO> bookings = List.of(responseDto);

        when(bookingService.getAllByRoomAndBetweenTwoDates(any(Room.class), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(bookings);

        mockMvc.perform(get("/booking/schedule")
                                .param("room", "ROOM1")
                                .param("dateFrom", "2024-04-20T10:00")
                                .param("dateTo", "2024-04-20T12:00"))
               .andExpect(status().isOk())
               .andExpect(content().json(objectMapper.writeValueAsString(bookings)));
    }

    @Test
    void cancelBooking_ShouldReturnNoContent() throws Exception {
        doNothing().when(bookingService).deactivateBooking(1);

        mockMvc.perform(patch("/booking/cancel/{id}", 1))
               .andExpect(status().isNoContent());
    }

   

}
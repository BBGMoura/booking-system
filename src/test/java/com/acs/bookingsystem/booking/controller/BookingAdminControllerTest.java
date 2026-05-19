package com.acs.bookingsystem.booking.controller;

import com.acs.bookingsystem.booking.entity.Booking;
import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.booking.request.BookingRequest;
import com.acs.bookingsystem.booking.service.BookingService;
import com.acs.bookingsystem.booking.view.BookingViewFactory;
import com.acs.bookingsystem.booking.view.ViewType;
import com.acs.bookingsystem.booking.view.dto.BookingDetail;
import com.acs.bookingsystem.danceclass.enums.ClassType;
import com.acs.bookingsystem.security.config.SecurityConfig;
import com.acs.bookingsystem.security.util.JwtUtil;
import com.acs.bookingsystem.user.UserTestData;
import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.enums.Role;
import com.acs.bookingsystem.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingAdminController.class)
@Import(SecurityConfig.class)
class BookingAdminControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private BookingService bookingService;
    @MockitoBean private BookingViewFactory viewFactory;
    @MockitoBean private UserService userService;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private AuthenticationProvider authenticationProvider;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final UUID BOOKING_UID = UUID.randomUUID();
    private static final UUID USER_UID = UserTestData.ADMIN_UUID;

    private final User adminUser = User.builder()
            .id(1L)
            .uid(USER_UID)
            .email("admin@example.com")
            .role(Role.ROLE_ADMIN)
            .locked(false)
            .enabled(true)
            .build();

    private final BookingDetail bookingDetail = new BookingDetail(
            BOOKING_UID, UUID.randomUUID(), Room.ASTAIRE,
            true, false, ClassType.PRIVATE,
            LocalDateTime.of(2025, 6, 2, 10, 0),
            LocalDateTime.of(2025, 6, 2, 11, 0),
            BigDecimal.TEN);

    @BeforeEach
    void setup() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                adminUser, null, adminUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void teardown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void givenNoAuth_whenAccessAdminEndpoint_thenReturns403() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/v1/admin/bookings/{bookingUid}", BOOKING_UID))
               .andExpect(status().isForbidden());
    }

    @Test
    void givenAdmin_whenGetBookingByUid_thenReturns200() throws Exception {
        when(bookingService.getBookingByUid(BOOKING_UID)).thenReturn(new Booking());
        when(viewFactory.createView(any(Booking.class), eq(Role.ROLE_ADMIN))).thenReturn(bookingDetail);

        mockMvc.perform(get("/api/v1/admin/bookings/{bookingUid}", BOOKING_UID))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.uid").value(BOOKING_UID.toString()));
    }

    @Test
    void givenAdmin_whenGetBookingsByUserUid_thenReturns200() throws Exception {
        when(bookingService.getAllBookingsByUserUid(eq(USER_UID), any(int.class), any(int.class)))
                .thenReturn(new PageImpl<>(List.of(new Booking())));
        when(viewFactory.createView(any(Booking.class), eq(ViewType.DETAIL))).thenReturn(bookingDetail);

        mockMvc.perform(get("/api/v1/admin/bookings/user/{userUid}", USER_UID))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void givenAdmin_whenCreateBooking_thenReturns201() throws Exception {
        BookingRequest request = new BookingRequest(Room.ASTAIRE, ClassType.PRIVATE, false,
                LocalDateTime.of(2025, 6, 2, 10, 0),
                LocalDateTime.of(2025, 6, 2, 11, 0));
        when(userService.getUserByUid(USER_UID)).thenReturn(adminUser);
        when(bookingService.createBooking(any(BookingRequest.class), any(User.class))).thenReturn(new Booking());
        when(viewFactory.createView(any(Booking.class), eq(ViewType.DETAIL))).thenReturn(bookingDetail);

        mockMvc.perform(post("/api/v1/admin/bookings")
                                .param("userUid", USER_UID.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isCreated());
    }

    @Test
    void givenAdmin_whenCancelBooking_thenReturns204() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/bookings/{bookingUid}", BOOKING_UID))
               .andExpect(status().isNoContent());

        verify(bookingService).deactivateBooking(BOOKING_UID);
    }
}

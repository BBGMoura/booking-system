package com.acs.bookingsystem.booking.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acs.bookingsystem.booking.BookingWithStatus;
import com.acs.bookingsystem.booking.entity.Booking;
import com.acs.bookingsystem.booking.enums.BookingStatusType;
import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.booking.request.BookingRequest;
import com.acs.bookingsystem.booking.service.BookingService;
import com.acs.bookingsystem.booking.view.BookingViewFactory;
import com.acs.bookingsystem.booking.view.ViewType;
import com.acs.bookingsystem.booking.view.dto.BookingDetail;
import com.acs.bookingsystem.booking.view.dto.BookingSummary;
import com.acs.bookingsystem.danceclass.enums.ClassType;
import com.acs.bookingsystem.security.config.SecurityConfig;
import com.acs.bookingsystem.security.util.JwtUtil;
import com.acs.bookingsystem.user.UserTestData;
import com.acs.bookingsystem.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
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

@WebMvcTest(BookingController.class)
@Import(SecurityConfig.class)
class BookingControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private BookingService bookingService;
  @MockitoBean private BookingViewFactory viewFactory;
  @MockitoBean private JwtUtil jwtUtil;
  @MockitoBean private AuthenticationProvider authenticationProvider;

  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
  private final User user = UserTestData.user;
  private static final UUID BOOKING_UID = UUID.randomUUID();

  private final BookingWithStatus bookingWithStatus =
      new BookingWithStatus(new Booking(), BookingStatusType.BOOKED);

  private final BookingDetail bookingDetail =
      new BookingDetail(
          BOOKING_UID,
          UUID.randomUUID(),
          Room.ASTAIRE,
          BookingStatusType.BOOKED,
          false,
          ClassType.PRIVATE,
          OffsetDateTime.of(2025, 6, 2, 10, 0, 0, 0, ZoneOffset.UTC),
          OffsetDateTime.of(2025, 6, 2, 11, 0, 0, 0, ZoneOffset.UTC),
          BigDecimal.TEN);

  private final BookingSummary bookingSummary =
      new BookingSummary(
          BOOKING_UID,
          Room.ASTAIRE,
          ClassType.PRIVATE,
          BookingStatusType.BOOKED,
          false,
          OffsetDateTime.of(2025, 6, 2, 10, 0, 0, 0, ZoneOffset.UTC),
          OffsetDateTime.of(2025, 6, 2, 11, 0, 0, 0, ZoneOffset.UTC));

  @BeforeEach
  void setup() {
    Authentication auth =
        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  void givenValidRequest_whenCreateBooking_thenReturns201() throws Exception {
    BookingRequest request =
        new BookingRequest(
            Room.ASTAIRE,
            ClassType.PRIVATE,
            false,
            OffsetDateTime.of(2025, 6, 2, 10, 0, 0, 0, ZoneOffset.UTC),
            OffsetDateTime.of(2025, 6, 2, 11, 0, 0, 0, ZoneOffset.UTC));

    when(bookingService.createBooking(any(BookingRequest.class), any(User.class)))
        .thenReturn(bookingWithStatus);
    when(viewFactory.createView(any(BookingWithStatus.class), eq(ViewType.DETAIL)))
        .thenReturn(bookingDetail);

    mockMvc
        .perform(
            post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.uid").value(BOOKING_UID.toString()));
  }

  @Test
  void givenValidBookingUid_whenGetOwnBooking_thenReturns200() throws Exception {
    when(bookingService.getBookingByUidAndUser(eq(BOOKING_UID), any(Long.class)))
        .thenReturn(bookingWithStatus);
    when(viewFactory.createView(any(BookingWithStatus.class), eq(ViewType.DETAIL)))
        .thenReturn(bookingDetail);

    mockMvc
        .perform(get("/api/v1/bookings/{bookingUid}", BOOKING_UID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.uid").value(BOOKING_UID.toString()));
  }

  @Test
  void givenValidRequest_whenGetBookings_thenReturns200() throws Exception {
    when(bookingService.getAllBookingsByUserId(any(Long.class), any(int.class), any(int.class)))
        .thenReturn(new PageImpl<>(List.of(bookingWithStatus)));
    when(viewFactory.createView(any(BookingWithStatus.class), eq(ViewType.DETAIL)))
        .thenReturn(bookingSummary);

    mockMvc
        .perform(get("/api/v1/bookings"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  void givenValidBookingUid_whenCancelBooking_thenReturns204() throws Exception {
    mockMvc
        .perform(delete("/api/v1/bookings/{bookingUid}", BOOKING_UID))
        .andExpect(status().isNoContent());

    verify(bookingService).cancelBookingByUserId(eq(BOOKING_UID), any(Long.class));
  }

  @Test
  void givenUnauthenticated_whenGetBookings_thenReturns403() throws Exception {
    SecurityContextHolder.clearContext();

    mockMvc.perform(get("/api/v1/bookings")).andExpect(status().isForbidden());
  }
}

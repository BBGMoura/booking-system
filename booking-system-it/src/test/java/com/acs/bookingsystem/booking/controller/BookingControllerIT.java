package com.acs.bookingsystem.booking.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.acs.bookingsystem.BaseIntegrationTest;
import com.acs.bookingsystem.booking.entity.Booking;
import com.acs.bookingsystem.booking.entity.BookingStatus;
import com.acs.bookingsystem.booking.enums.BookingStatusType;
import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.booking.repository.BookingRepository;
import com.acs.bookingsystem.booking.repository.BookingStatusRepository;
import com.acs.bookingsystem.danceclass.entity.DanceClass;
import com.acs.bookingsystem.danceclass.enums.ClassType;
import com.acs.bookingsystem.danceclass.repository.DanceClassRepository;
import com.acs.bookingsystem.security.util.JwtUtil;
import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.enums.Role;
import com.acs.bookingsystem.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookingControllerIT extends BaseIntegrationTest {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private UserRepository userRepository;
  @Autowired private JwtUtil jwtUtil;
  @Autowired private BookingRepository bookingRepository;
  @Autowired private BookingStatusRepository bookingStatusRepository;
  @Autowired private DanceClassRepository danceClassRepository;
  @Autowired private ObjectMapper objectMapper;

  private String createUserAndGetToken() {
    return jwtUtil.generateToken(saveUser(Role.ROLE_USER, null, null));
  }

  private User saveUser(Role role, String firstName, String lastName) {
    return userRepository.save(
        User.builder()
            .uid(UUID.randomUUID())
            .email(UUID.randomUUID() + "@test.com")
            .role(role)
            .firstName(firstName)
            .lastName(lastName)
            .locked(false)
            .enabled(true)
            .build());
  }

  private Booking saveBookedBooking(User booker, Room room, OffsetDateTime from) {
    DanceClass danceClass =
        danceClassRepository.save(
            new DanceClass(null, ClassType.PRIVATE, false, BigDecimal.TEN, Role.ROLE_USER));
    Booking booking =
        bookingRepository.save(
            Booking.builder()
                .user(booker)
                .room(room)
                .danceClass(danceClass)
                .shareable(false)
                .bookedFrom(from)
                .bookedTo(from.plusHours(1))
                .totalPrice(BigDecimal.TEN)
                .build());
    bookingStatusRepository.save(
        BookingStatus.builder()
            .booking(booking)
            .status(BookingStatusType.BOOKED)
            .createdBy(booker)
            .build());
    return booking;
  }

  private JsonNode get(String url, String token) throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    ResponseEntity<String> response =
        restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    return objectMapper.readTree(response.getBody());
  }

  private String scheduleUrl(Room room, OffsetDateTime day) {
    return "/api/v1/bookings/schedule?room="
        + room
        + "&dateFrom="
        + day
        + "&dateTo="
        + day.plusDays(1);
  }

  @Test
  void givenAdmin_whenGetBookingSchedule_thenEachBookingIncludesBookerName() throws Exception {
    OffsetDateTime day = OffsetDateTime.of(2030, 1, 7, 0, 0, 0, 0, ZoneOffset.UTC);
    User ginger = saveUser(Role.ROLE_USER, "Ginger", "Rogers");
    User fred = saveUser(Role.ROLE_USER, "Fred", "Astaire");
    Booking gingerBooking = saveBookedBooking(ginger, Room.ASTAIRE, day.withHour(10));
    Booking fredBooking = saveBookedBooking(fred, Room.ASTAIRE, day.withHour(12));
    String adminToken = jwtUtil.generateToken(saveUser(Role.ROLE_ADMIN, null, null));

    JsonNode schedule = get(scheduleUrl(Room.ASTAIRE, day), adminToken);

    assertThat(schedule).hasSize(2);
    assertThat(schedule.get(0).get("uid").asText()).isEqualTo(gingerBooking.getUid().toString());
    assertThat(schedule.get(0).get("userUid").asText()).isEqualTo(ginger.getUid().toString());
    assertThat(schedule.get(0).get("firstName").asText()).isEqualTo("Ginger");
    assertThat(schedule.get(0).get("lastName").asText()).isEqualTo("Rogers");
    assertThat(schedule.get(1).get("uid").asText()).isEqualTo(fredBooking.getUid().toString());
    assertThat(schedule.get(1).get("firstName").asText()).isEqualTo("Fred");
    assertThat(schedule.get(1).get("lastName").asText()).isEqualTo("Astaire");
  }

  @Test
  void givenAdmin_whenGetBookingScheduleForBookerWithoutName_thenNameFieldsAreNull()
      throws Exception {
    OffsetDateTime day = OffsetDateTime.of(2030, 1, 8, 0, 0, 0, 0, ZoneOffset.UTC);
    User unnamedAdmin = saveUser(Role.ROLE_ADMIN, null, null);
    saveBookedBooking(unnamedAdmin, Room.ASTAIRE, day.withHour(10));

    JsonNode schedule = get(scheduleUrl(Room.ASTAIRE, day), jwtUtil.generateToken(unnamedAdmin));

    assertThat(schedule).hasSize(1);
    assertThat(schedule.get(0).has("firstName")).isTrue();
    assertThat(schedule.get(0).get("firstName").isNull()).isTrue();
    assertThat(schedule.get(0).has("lastName")).isTrue();
    assertThat(schedule.get(0).get("lastName").isNull()).isTrue();
  }

  @Test
  void givenUser_whenGetBookingSchedule_thenBookerIdentityIsNotExposed() throws Exception {
    OffsetDateTime day = OffsetDateTime.of(2030, 1, 9, 0, 0, 0, 0, ZoneOffset.UTC);
    User ginger = saveUser(Role.ROLE_USER, "Ginger", "Rogers");
    Booking booking = saveBookedBooking(ginger, Room.ASTAIRE, day.withHour(10));

    JsonNode schedule = get(scheduleUrl(Room.ASTAIRE, day), createUserAndGetToken());

    assertThat(schedule).hasSize(1);
    assertThat(schedule.get(0).get("uid").asText()).isEqualTo(booking.getUid().toString());
    assertThat(schedule.get(0).has("userUid")).isFalse();
    assertThat(schedule.get(0).has("firstName")).isFalse();
    assertThat(schedule.get(0).has("lastName")).isFalse();
  }

  @Test
  void givenAdmin_whenGetBookingByUid_thenIncludesBookerName() throws Exception {
    User ginger = saveUser(Role.ROLE_USER, "Ginger", "Rogers");
    Booking booking =
        saveBookedBooking(
            ginger, Room.ASTAIRE, OffsetDateTime.of(2030, 1, 10, 10, 0, 0, 0, ZoneOffset.UTC));
    String adminToken = jwtUtil.generateToken(saveUser(Role.ROLE_ADMIN, null, null));

    JsonNode detail = get("/api/v1/admin/bookings/" + booking.getUid(), adminToken);

    assertThat(detail.get("firstName").asText()).isEqualTo("Ginger");
    assertThat(detail.get("lastName").asText()).isEqualTo("Rogers");
  }

  @Test
  void givenAdmin_whenGetBookingsByUserUid_thenIncludesBookerName() throws Exception {
    User ginger = saveUser(Role.ROLE_USER, "Ginger", "Rogers");
    saveBookedBooking(
        ginger, Room.ASTAIRE, OffsetDateTime.of(2030, 1, 11, 10, 0, 0, 0, ZoneOffset.UTC));
    String adminToken = jwtUtil.generateToken(saveUser(Role.ROLE_ADMIN, null, null));

    JsonNode page = get("/api/v1/admin/bookings/user/" + ginger.getUid(), adminToken);

    assertThat(page.get("content")).hasSize(1);
    assertThat(page.get("content").get(0).get("firstName").asText()).isEqualTo("Ginger");
    assertThat(page.get("content").get(0).get("lastName").asText()).isEqualTo("Rogers");
  }

  @Test
  void givenUser_whenGetOwnBooking_thenIncludesOwnName() throws Exception {
    User ginger = saveUser(Role.ROLE_USER, "Ginger", "Rogers");
    Booking booking =
        saveBookedBooking(
            ginger, Room.ASTAIRE, OffsetDateTime.of(2030, 1, 12, 10, 0, 0, 0, ZoneOffset.UTC));

    JsonNode detail = get("/api/v1/bookings/" + booking.getUid(), jwtUtil.generateToken(ginger));

    assertThat(detail.get("firstName").asText()).isEqualTo("Ginger");
    assertThat(detail.get("lastName").asText()).isEqualTo("Rogers");
  }

  @Test
  void givenInvalidOrderValue_whenGetBookings_thenReturns400NotForbidden() {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(createUserAndGetToken());

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/api/v1/bookings?order=sideways",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }
}

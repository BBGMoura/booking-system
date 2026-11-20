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

  private static final OffsetDateTime NOW =
      OffsetDateTime.of(2030, 6, 15, 12, 0, 0, 0, ZoneOffset.UTC);

  private void cancel(Booking booking, User by) {
    bookingStatusRepository.save(
        BookingStatus.builder()
            .booking(booking)
            .status(BookingStatusType.CANCELLED)
            .createdBy(by)
            .build());
  }

  private java.util.List<String> uids(JsonNode page) {
    java.util.List<String> result = new java.util.ArrayList<>();
    page.get("content").forEach(n -> result.add(n.get("uid").asText()));
    return result;
  }

  private JsonNode adminGetUserBookings(User user, String query) throws Exception {
    String adminToken = jwtUtil.generateToken(saveUser(Role.ROLE_ADMIN, null, null));
    return get("/api/v1/admin/bookings/user/" + user.getUid() + query, adminToken);
  }

  @Test
  void givenAdmin_whenGetUserBookingsWithDateFromAndAsc_thenReturnsUpcomingSoonestFirst()
      throws Exception {
    User ginger = saveUser(Role.ROLE_USER, "Ginger", "Rogers");
    saveBookedBooking(ginger, Room.ASTAIRE, NOW.minusDays(1));
    Booking later = saveBookedBooking(ginger, Room.ASTAIRE, NOW.plusDays(5));
    Booking sooner = saveBookedBooking(ginger, Room.ASTAIRE, NOW.plusDays(2));

    JsonNode page = adminGetUserBookings(ginger, "?dateFrom=" + NOW + "&order=ASC");

    assertThat(uids(page)).containsExactly(sooner.getUid().toString(), later.getUid().toString());
  }

  @Test
  void givenAdmin_whenGetUserBookingsWithDateToAndDesc_thenReturnsPastMostRecentFirst()
      throws Exception {
    User ginger = saveUser(Role.ROLE_USER, "Ginger", "Rogers");
    saveBookedBooking(ginger, Room.ASTAIRE, NOW.plusDays(1));
    Booking older = saveBookedBooking(ginger, Room.ASTAIRE, NOW.minusDays(5));
    Booking recent = saveBookedBooking(ginger, Room.ASTAIRE, NOW.minusDays(2));

    JsonNode page = adminGetUserBookings(ginger, "?dateTo=" + NOW + "&order=DESC");

    assertThat(uids(page)).containsExactly(recent.getUid().toString(), older.getUid().toString());
  }

  @Test
  void givenAdmin_whenGetUserBookingsWithDateFromAndStatus_thenCountsOnlyBookedUpcoming()
      throws Exception {
    User ginger = saveUser(Role.ROLE_USER, "Ginger", "Rogers");
    Booking booked = saveBookedBooking(ginger, Room.ASTAIRE, NOW.plusDays(2));
    Booking cancelled = saveBookedBooking(ginger, Room.ASTAIRE, NOW.plusDays(3));
    cancel(cancelled, ginger);
    saveBookedBooking(ginger, Room.ASTAIRE, NOW.minusDays(2));

    JsonNode page = adminGetUserBookings(ginger, "?dateFrom=" + NOW + "&status=BOOKED");

    assertThat(uids(page)).containsExactly(booked.getUid().toString());
    assertThat(page.get("page").get("totalElements").asInt()).isEqualTo(1);
  }

  @Test
  void givenAdmin_whenGetUserBookingsWithNoFilters_thenReturnsOnlyThatUsersNewestFirstWithSize10()
      throws Exception {
    User ginger = saveUser(Role.ROLE_USER, "Ginger", "Rogers");
    User fred = saveUser(Role.ROLE_USER, "Fred", "Astaire");
    saveBookedBooking(fred, Room.ASTAIRE, NOW);
    for (int i = 0; i < 11; i++) {
      saveBookedBooking(ginger, Room.ASTAIRE, NOW.plusDays(i));
    }

    JsonNode page = adminGetUserBookings(ginger, "");

    assertThat(page.get("content")).hasSize(10);
    assertThat(page.get("page").get("size").asInt()).isEqualTo(10);
    assertThat(page.get("page").get("totalElements").asInt()).isEqualTo(11);
    assertThat(page.get("content").get(0).get("dateFrom").asText()).startsWith("2030-06-25");
  }

  @Test
  void givenNonAdmin_whenGetUserBookings_thenReturns403() {
    User ginger = saveUser(Role.ROLE_USER, "Ginger", "Rogers");
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(createUserAndGetToken());

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/api/v1/admin/bookings/user/" + ginger.getUid(),
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
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

  // --- validate vs create: same verdict ---

  private void ensureActivePrivateClass() {
    if (danceClassRepository
        .findByActiveIsTrueAndClassTypeAndRole(ClassType.PRIVATE, Role.ROLE_USER)
        .isEmpty()) {
      danceClassRepository.save(
          new DanceClass(null, ClassType.PRIVATE, true, BigDecimal.TEN, Role.ROLE_USER));
    }
  }

  private ResponseEntity<String> postBooking(
      String path, String token, boolean shareable, OffsetDateTime from, OffsetDateTime to) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
    String body =
        String.format(
            "{\"room\":\"%s\",\"classType\":\"PRIVATE\",\"isShareable\":%s,"
                + "\"dateFrom\":\"%s\",\"dateTo\":\"%s\"}",
            Room.ASTAIRE, shareable, from, to);
    return restTemplate.exchange(
        path, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
  }

  private void assertValidateMatchesCreate(
      OffsetDateTime from, OffsetDateTime to, boolean shareable, String expectedError)
      throws Exception {
    ensureActivePrivateClass();
    String token = createUserAndGetToken();

    ResponseEntity<String> validate =
        postBooking("/api/v1/bookings/validate", token, shareable, from, to);
    ResponseEntity<String> create = postBooking("/api/v1/bookings", token, shareable, from, to);

    if (expectedError == null) {
      assertThat(validate.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
      assertThat(create.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    } else {
      assertThat(validate.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(create.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      JsonNode validateBody = objectMapper.readTree(validate.getBody());
      JsonNode createBody = objectMapper.readTree(create.getBody());
      assertThat(validateBody.get("error").asText()).isEqualTo(expectedError);
      assertThat(validateBody.get("error")).isEqualTo(createBody.get("error"));
      assertThat(validateBody.get("message")).isEqualTo(createBody.get("message"));
    }
  }

  private void saveShareableBooking(OffsetDateTime from) {
    User booker = saveUser(Role.ROLE_USER, null, null);
    DanceClass danceClass =
        danceClassRepository.save(
            new DanceClass(null, ClassType.PRIVATE, false, BigDecimal.TEN, Role.ROLE_USER));
    Booking booking =
        bookingRepository.save(
            Booking.builder()
                .user(booker)
                .room(Room.ASTAIRE)
                .danceClass(danceClass)
                .shareable(true)
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
  }

  @Test
  void givenFreeSlot_whenValidateBooking_thenReturns204LikeCreate() throws Exception {
    OffsetDateTime from = OffsetDateTime.of(2032, 5, 3, 10, 0, 0, 0, ZoneOffset.UTC);
    assertValidateMatchesCreate(from, from.plusHours(1), false, null);
  }

  @Test
  void givenValidatedSlot_whenValidateBooking_thenNothingIsSaved() {
    ensureActivePrivateClass();
    OffsetDateTime from = OffsetDateTime.of(2032, 5, 4, 10, 0, 0, 0, ZoneOffset.UTC);
    long before = bookingRepository.count();

    ResponseEntity<String> response =
        postBooking(
            "/api/v1/bookings/validate", createUserAndGetToken(), false, from, from.plusHours(1));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(bookingRepository.count()).isEqualTo(before);
  }

  @Test
  void givenNonShareableOverlapsExisting_whenValidateBooking_thenConflictLikeCreate()
      throws Exception {
    OffsetDateTime from = OffsetDateTime.of(2032, 5, 5, 10, 0, 0, 0, ZoneOffset.UTC);
    saveShareableBooking(from);
    assertValidateMatchesCreate(
        from.plusMinutes(30), from.plusMinutes(90), false, "BOOKING_CONFLICT");
  }

  @Test
  void givenShareableOverlapsNonShareable_whenValidateBooking_thenConflictLikeCreate()
      throws Exception {
    OffsetDateTime from = OffsetDateTime.of(2032, 5, 6, 10, 0, 0, 0, ZoneOffset.UTC);
    saveBookedBooking(saveUser(Role.ROLE_USER, null, null), Room.ASTAIRE, from);
    assertValidateMatchesCreate(
        from.plusMinutes(30), from.plusMinutes(90), true, "BOOKING_CONFLICT");
  }

  @Test
  void givenThreeShareableOverlap_whenValidateBooking_thenShareableLimitLikeCreate()
      throws Exception {
    OffsetDateTime from = OffsetDateTime.of(2032, 5, 7, 10, 0, 0, 0, ZoneOffset.UTC);
    saveShareableBooking(from);
    saveShareableBooking(from);
    saveShareableBooking(from);
    assertValidateMatchesCreate(from, from.plusHours(1), true, "BOOKING_SHAREABLE_LIMIT");
  }

  @Test
  void givenInvalidTime_whenValidateBooking_thenTimeInvalidLikeCreate() throws Exception {
    OffsetDateTime from = OffsetDateTime.of(2032, 5, 10, 10, 3, 0, 0, ZoneOffset.UTC);
    assertValidateMatchesCreate(from, from.plusHours(1), false, "BOOKING_TIME_INVALID");
  }
}

package com.acs.bookingsystem.settings.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.acs.bookingsystem.BaseIntegrationTest;
import com.acs.bookingsystem.booking.entity.Booking;
import com.acs.bookingsystem.booking.entity.BookingStatus;
import com.acs.bookingsystem.booking.enums.BookingStatusType;
import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.booking.repository.BookingRepository;
import com.acs.bookingsystem.booking.repository.BookingStatusRepository;
import com.acs.bookingsystem.booking.request.BookingRequest;
import com.acs.bookingsystem.booking.service.validation.BookingValidator;
import com.acs.bookingsystem.booking.service.validation.ValidationFailure;
import com.acs.bookingsystem.common.exception.model.ErrorCode;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StudioSettingsIT extends BaseIntegrationTest {

  private static final String ADMIN_URL = "/api/v1/admin/settings";
  private static final String PUBLIC_URL = "/api/v1/settings/public";

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private UserRepository userRepository;
  @Autowired private JwtUtil jwtUtil;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private JdbcTemplate jdbc;
  @Autowired private TransactionTemplate transactionTemplate;
  @Autowired private BookingValidator bookingValidator;
  @Autowired private BookingRepository bookingRepository;
  @Autowired private BookingStatusRepository bookingStatusRepository;
  @Autowired private DanceClassRepository danceClassRepository;

  @BeforeEach
  @AfterEach
  void restoreDefaults() {
    // Other ITs share this database and expect the migration's defaults. Hikari runs with
    // auto-commit off, so a bare JdbcTemplate write would never be committed.
    transactionTemplate.executeWithoutResult(
        status ->
            jdbc.update(
                "UPDATE studio_settings SET studio_name = 'ACS Dance Studio',"
                    + " recurring_max_dates = 5, recurring_max_active = 3, shared_room_max = 3,"
                    + " shared_discount_per_hour = 0.00, cancellation_window_hours = 24,"
                    + " payment_due_days = 14, billing_day = 1, invoice_prefix = 'ACS',"
                    + " business_name = NULL, business_address = NULL, bank_account_name = NULL,"
                    + " bank_sort_code = NULL, bank_account_number = NULL WHERE id = 1"));
  }

  private User saveUser(Role role) {
    return userRepository.save(
        User.builder()
            .uid(UUID.randomUUID())
            .email(UUID.randomUUID() + "@test.com")
            .role(role)
            .locked(false)
            .enabled(true)
            .build());
  }

  private ResponseEntity<String> send(
      String url, HttpMethod method, User user, Map<String, Object> body) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(jwtUtil.generateToken(user));
    headers.setContentType(MediaType.APPLICATION_JSON);
    return restTemplate.exchange(url, method, new HttpEntity<>(body, headers), String.class);
  }

  private Map<String, Object> body(int sharedRoomMax) {
    return Map.ofEntries(
        Map.entry("studioName", "  Round Trip Studio  "),
        Map.entry("recurringMaxDates", 10),
        Map.entry("recurringMaxActive", 4),
        Map.entry("sharedRoomMax", sharedRoomMax),
        Map.entry("sharedDiscountPerHour", 2.5),
        Map.entry("cancellationWindowHours", 48),
        Map.entry("paymentDueDays", 30),
        Map.entry("billingDay", 15),
        Map.entry("invoicePrefix", "INV"),
        Map.entry("bankAccountName", "ACS Ltd"),
        Map.entry("bankSortCode", "12-34-56"),
        Map.entry("bankAccountNumber", "12345678"));
  }

  @Test
  void migrationInsertsExactlyOneRowWithDefaults() {
    Map<String, Object> row = jdbc.queryForMap("SELECT * FROM studio_settings");

    assertThat(jdbc.queryForObject("SELECT count(*) FROM studio_settings", Integer.class))
        .isEqualTo(1);
    assertThat(row).containsEntry("studio_name", "ACS Dance Studio");
    assertThat(row).containsEntry("recurring_max_dates", 5);
    assertThat(row).containsEntry("recurring_max_active", 3);
    assertThat(row).containsEntry("invoice_prefix", "ACS");
    assertThat(row).containsEntry("billing_day", 1);
    assertThat(row).containsEntry("payment_due_days", 14);
    assertThat(row).containsEntry("cancellation_window_hours", 24);
  }

  @Test
  void secondRowIsRejectedByTheDatabase() {
    try {
      jdbc.update("INSERT INTO studio_settings (id) VALUES (2)");
      throw new AssertionError("expected the single-row CHECK to reject id 2");
    } catch (DataIntegrityViolationException expected) {
      assertThat(jdbc.queryForObject("SELECT count(*) FROM studio_settings", Integer.class))
          .isEqualTo(1);
    }
  }

  @Test
  void duplicateOfRowOneIsRejectedByTheDatabase() {
    try {
      jdbc.update("INSERT INTO studio_settings (id) VALUES (1)");
      throw new AssertionError("expected the primary key to reject a second id 1");
    } catch (DataIntegrityViolationException expected) {
      assertThat(jdbc.queryForObject("SELECT count(*) FROM studio_settings", Integer.class))
          .isEqualTo(1);
    }
  }

  @Test
  void databaseRejectsOutOfRangeValues() {
    try {
      jdbc.update("UPDATE studio_settings SET shared_room_max = 1 WHERE id = 1");
      throw new AssertionError("expected CHECK to reject shared_room_max 1");
    } catch (DataIntegrityViolationException expected) {
      assertThat(expected).isNotNull();
    }
  }

  @Test
  void adminRoundTrip_putThenGetReturnsNormalisedValuesAndRecordsWho() throws Exception {
    User admin = saveUser(Role.ROLE_ADMIN);

    ResponseEntity<String> put = send(ADMIN_URL, HttpMethod.PUT, admin, body(4));
    assertThat(put.getStatusCode()).isEqualTo(HttpStatus.OK);

    ResponseEntity<String> get = send(ADMIN_URL, HttpMethod.GET, admin, null);
    JsonNode json = objectMapper.readTree(get.getBody());
    assertThat(json.get("studioName").asText()).isEqualTo("Round Trip Studio");
    assertThat(json.get("sharedRoomMax").asInt()).isEqualTo(4);
    assertThat(json.get("sharedDiscountPerHour").decimalValue()).isEqualByComparingTo("2.50");
    assertThat(json.get("billingDay").asInt()).isEqualTo(15);
    assertThat(json.get("bankSortCode").asText()).isEqualTo("123456");
    assertThat(json.get("bankAccountNumber").asText()).isEqualTo("12345678");
    assertThat(json.get("businessName").isNull()).isTrue();
    assertThat(jdbc.queryForObject("SELECT count(*) FROM studio_settings", Integer.class))
        .isEqualTo(1);
    assertThat(
            jdbc.queryForObject(
                "SELECT updated_by_id FROM studio_settings WHERE id = 1", Long.class))
        .isEqualTo(admin.getId());
  }

  @Test
  void invalidPutReturns400AndChangesNothing() {
    User admin = saveUser(Role.ROLE_ADMIN);
    Map<String, Object> invalid = new java.util.HashMap<>(body(4));
    invalid.put("bankSortCode", "12345");

    ResponseEntity<String> put = send(ADMIN_URL, HttpMethod.PUT, admin, invalid);

    assertThat(put.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(
            jdbc.queryForObject(
                "SELECT studio_name FROM studio_settings WHERE id = 1", String.class))
        .isEqualTo("ACS Dance Studio");
  }

  @Test
  void userGets403OnAdminEndpoints() {
    User user = saveUser(Role.ROLE_USER);

    assertThat(send(ADMIN_URL, HttpMethod.GET, user, null).getStatusCode())
        .isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(send(ADMIN_URL, HttpMethod.PUT, user, body(4)).getStatusCode())
        .isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void publicEndpointNeverIncludesBankDetails() throws Exception {
    send(ADMIN_URL, HttpMethod.PUT, saveUser(Role.ROLE_ADMIN), body(4));

    ResponseEntity<String> response =
        send(PUBLIC_URL, HttpMethod.GET, saveUser(Role.ROLE_USER), null);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode json = objectMapper.readTree(response.getBody());
    assertThat(json.size()).isEqualTo(5);
    assertThat(json.get("studioName").asText()).isEqualTo("Round Trip Studio");
    assertThat(json.get("sharedRoomMax").asInt()).isEqualTo(4);
    assertThat(response.getBody()).doesNotContain("bank").doesNotContain("12345678");
  }

  @Test
  void sharedRoomLimitFollowsTheSetting() {
    User booker = saveUser(Role.ROLE_USER);
    OffsetDateTime start = nextMonday().atTime(12, 0).atOffset(ZoneOffset.UTC);
    for (int i = 0; i < 3; i++) {
      saveSharedBooking(booker, start);
    }
    BookingRequest request =
        new BookingRequest(Room.FOSSE, ClassType.PRACTICE, true, start, start.plusHours(1));

    Optional<ValidationFailure> atDefault = bookingValidator.validate(request);
    assertThat(atDefault).isPresent();
    assertThat(atDefault.get().code()).isEqualTo(ErrorCode.BOOKING_SHAREABLE_LIMIT);

    send(ADMIN_URL, HttpMethod.PUT, saveUser(Role.ROLE_ADMIN), body(4));
    assertThat(bookingValidator.validate(request)).isEmpty();

    send(ADMIN_URL, HttpMethod.PUT, saveUser(Role.ROLE_ADMIN), body(2));
    Optional<ValidationFailure> lowered = bookingValidator.validate(request);
    assertThat(lowered).isPresent();
    assertThat(lowered.get().message()).contains("2");
  }

  private LocalDate nextMonday() {
    return LocalDate.of(2031, Month.JUNE, 2).with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
  }

  private void saveSharedBooking(User booker, OffsetDateTime from) {
    DanceClass danceClass =
        danceClassRepository.save(
            new DanceClass(ClassType.PRACTICE, false, BigDecimal.TEN, Role.ROLE_USER));
    Booking booking =
        bookingRepository.save(
            Booking.builder()
                .bookingType(ClassType.PRACTICE)
                .bookedFor(booker)
                .createdBy(booker)
                .room(Room.FOSSE)
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
}

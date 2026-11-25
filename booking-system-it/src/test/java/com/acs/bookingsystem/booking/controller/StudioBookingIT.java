package com.acs.bookingsystem.booking.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.acs.bookingsystem.BaseIntegrationTest;
import com.acs.bookingsystem.booking.enums.Room;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StudioBookingIT extends BaseIntegrationTest {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private UserRepository userRepository;
  @Autowired private DanceClassRepository danceClassRepository;
  @Autowired private JwtUtil jwtUtil;
  @Autowired private ObjectMapper objectMapper;

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

  private void ensureActivePrivateClass() {
    if (danceClassRepository
        .findByActiveIsTrueAndClassTypeAndRole(ClassType.PRIVATE, Role.ROLE_USER)
        .isEmpty()) {
      danceClassRepository.save(
          new DanceClass(null, ClassType.PRIVATE, true, BigDecimal.TEN, Role.ROLE_USER));
    }
  }

  private ResponseEntity<String> send(HttpMethod method, String path, String token, String body) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    headers.setContentType(MediaType.APPLICATION_JSON);
    return restTemplate.exchange(path, method, new HttpEntity<>(body, headers), String.class);
  }

  private ResponseEntity<String> book(
      String token, Room room, String type, boolean shareable, OffsetDateTime from) {
    String body =
        String.format(
            "{\"room\":\"%s\",\"classType\":\"%s\",\"isShareable\":%s,"
                + "\"dateFrom\":\"%s\",\"dateTo\":\"%s\"}",
            room, type, shareable, from, from.plusHours(1));
    return send(HttpMethod.POST, "/api/v1/bookings", token, body);
  }

  private JsonNode json(ResponseEntity<String> response) throws Exception {
    return objectMapper.readTree(response.getBody());
  }

  @Test
  void adminBooksClosedForTheStudio_thenItBlocksAUsersBookingButIsNeverInMyBookings()
      throws Exception {
    ensureActivePrivateClass();
    OffsetDateTime from = OffsetDateTime.of(2033, 3, 7, 10, 0, 0, 0, ZoneOffset.UTC);
    User admin = saveUser(Role.ROLE_ADMIN);
    User user = saveUser(Role.ROLE_USER);
    String adminToken = jwtUtil.generateToken(admin);
    String userToken = jwtUtil.generateToken(user);

    ResponseEntity<String> closed = book(adminToken, Room.BUSSELL, "UNAVAILABLE", false, from);

    assertThat(closed.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(json(closed).get("forStudio").asBoolean()).isTrue();
    assertThat(json(closed).get("userUid").isNull()).isTrue();
    assertThat(json(closed).get("classType").asText()).isEqualTo("UNAVAILABLE");
    assertThat(json(closed).get("totalPrice").decimalValue()).isEqualByComparingTo("0");

    ResponseEntity<String> clash = book(userToken, Room.BUSSELL, "PRIVATE", false, from);
    assertThat(clash.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

    ResponseEntity<String> shareableClash =
        book(userToken, Room.BUSSELL, "PRIVATE", true, from.plusMinutes(30));
    assertThat(shareableClash.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

    JsonNode mine = json(send(HttpMethod.GET, "/api/v1/bookings", userToken, null));
    assertThat(mine.get("content")).isEmpty();
    JsonNode adminsMine = json(send(HttpMethod.GET, "/api/v1/bookings", adminToken, null));
    assertThat(adminsMine.get("content")).isEmpty();

    String scheduleUrl =
        "/api/v1/bookings/schedule?room=BUSSELL&dateFrom="
            + from.minusHours(1)
            + "&dateTo="
            + from.plusDays(1);
    JsonNode adminSchedule = json(send(HttpMethod.GET, scheduleUrl, adminToken, null));
    assertThat(adminSchedule).hasSize(1);
    assertThat(adminSchedule.get(0).get("forStudio").asBoolean()).isTrue();
    assertThat(adminSchedule.get(0).get("userUid").isNull()).isTrue();
    JsonNode userSchedule = json(send(HttpMethod.GET, scheduleUrl, userToken, null));
    assertThat(userSchedule).hasSize(1);
    assertThat(userSchedule.get(0).get("forStudio").asBoolean()).isTrue();

    JsonNode usersBookings =
        json(
            send(HttpMethod.GET, "/api/v1/admin/bookings/user/" + user.getUid(), adminToken, null));
    assertThat(usersBookings.get("content")).isEmpty();
  }

  @Test
  void adminCanCancelAStudioBooking() throws Exception {
    OffsetDateTime from = OffsetDateTime.of(2033, 3, 8, 10, 0, 0, 0, ZoneOffset.UTC);
    String adminToken = jwtUtil.generateToken(saveUser(Role.ROLE_ADMIN));
    String uid = json(book(adminToken, Room.FOSSE, "UNAVAILABLE", false, from)).get("uid").asText();

    ResponseEntity<String> cancel =
        send(HttpMethod.DELETE, "/api/v1/admin/bookings/" + uid, adminToken, null);

    assertThat(cancel.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }

  @Test
  void userAskingForClosedGets403() {
    OffsetDateTime from = OffsetDateTime.of(2033, 3, 9, 10, 0, 0, 0, ZoneOffset.UTC);
    String userToken = jwtUtil.generateToken(saveUser(Role.ROLE_USER));

    assertThat(book(userToken, Room.FOSSE, "UNAVAILABLE", false, from).getStatusCode())
        .isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void sharedClosedGets400() {
    OffsetDateTime from = OffsetDateTime.of(2033, 3, 10, 10, 0, 0, 0, ZoneOffset.UTC);
    String adminToken = jwtUtil.generateToken(saveUser(Role.ROLE_ADMIN));

    assertThat(book(adminToken, Room.FOSSE, "UNAVAILABLE", true, from).getStatusCode())
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void groupClassGets400ForAnAdminToo() {
    OffsetDateTime from = OffsetDateTime.of(2033, 3, 11, 10, 0, 0, 0, ZoneOffset.UTC);
    String adminToken = jwtUtil.generateToken(saveUser(Role.ROLE_ADMIN));

    assertThat(book(adminToken, Room.FOSSE, "GROUP", false, from).getStatusCode())
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void classTypesIncludeClosedOnlyForAdminsAndNeverGroup() throws Exception {
    ensureActivePrivateClass();
    danceClassRepository.save(
        new DanceClass(null, ClassType.PRACTICE, true, BigDecimal.ONE, Role.ROLE_ADMIN));

    JsonNode forUser =
        json(
            send(
                HttpMethod.GET,
                "/api/v1/dance-classes/class-types",
                jwtUtil.generateToken(saveUser(Role.ROLE_USER)),
                null));
    JsonNode forAdmin =
        json(
            send(
                HttpMethod.GET,
                "/api/v1/dance-classes/class-types",
                jwtUtil.generateToken(saveUser(Role.ROLE_ADMIN)),
                null));

    assertThat(forUser.toString()).contains("PRIVATE").doesNotContain("UNAVAILABLE", "GROUP");
    assertThat(forAdmin.toString()).contains("PRIVATE", "UNAVAILABLE").doesNotContain("GROUP");
  }
}

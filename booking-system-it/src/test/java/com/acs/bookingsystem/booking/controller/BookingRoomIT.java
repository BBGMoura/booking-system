package com.acs.bookingsystem.booking.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.acs.bookingsystem.BaseIntegrationTest;
import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.booking.request.BookingRequest;
import com.acs.bookingsystem.danceclass.entity.DanceClass;
import com.acs.bookingsystem.danceclass.enums.ClassType;
import com.acs.bookingsystem.danceclass.repository.DanceClassRepository;
import com.acs.bookingsystem.security.util.JwtUtil;
import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.enums.Role;
import com.acs.bookingsystem.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Books through the real endpoints so a mismatch between the {@link Room} enum and the database
 * check constraint fails here instead of in production.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookingRoomIT extends BaseIntegrationTest {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private UserRepository userRepository;
  @Autowired private DanceClassRepository danceClassRepository;
  @Autowired private JwtUtil jwtUtil;

  @BeforeEach
  void ensureActivePrivateClass() {
    // Other tests save inactive classes only; a second active one would make the lookup ambiguous.
    if (danceClassRepository
        .findByActiveIsTrueAndClassTypeAndRole(ClassType.PRIVATE, Role.ROLE_USER)
        .isEmpty()) {
      danceClassRepository.save(
          new DanceClass(ClassType.PRIVATE, true, BigDecimal.TEN, Role.ROLE_USER));
    }
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

  private ResponseEntity<String> post(String url, User caller, Room room, int dayOfMonth) {
    OffsetDateTime from = OffsetDateTime.of(2031, 3, dayOfMonth, 10, 0, 0, 0, ZoneOffset.UTC);
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(jwtUtil.generateToken(caller));
    BookingRequest body =
        new BookingRequest(room, ClassType.PRIVATE, false, from, from.plusHours(1));
    return restTemplate.exchange(
        url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
  }

  @ParameterizedTest
  @EnumSource(Room.class)
  void givenUser_whenCreateBookingInAnyRoom_thenReturns201(Room room) {
    ResponseEntity<String> response = post("/api/v1/bookings", saveUser(Role.ROLE_USER), room, 5);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  }

  @ParameterizedTest
  @EnumSource(Room.class)
  void givenAdmin_whenCreateBookingInAnyRoom_thenReturns201(Room room) {
    User booker = saveUser(Role.ROLE_USER);

    ResponseEntity<String> response =
        post(
            "/api/v1/admin/bookings?userUid=" + booker.getUid(),
            saveUser(Role.ROLE_ADMIN),
            room,
            6);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  }
}

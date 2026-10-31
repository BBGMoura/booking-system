package com.acs.bookingsystem.booking.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.acs.bookingsystem.BaseIntegrationTest;
import com.acs.bookingsystem.security.util.JwtUtil;
import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.enums.Role;
import com.acs.bookingsystem.user.repository.UserRepository;
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

  private String createUserAndGetToken() {
    User user =
        userRepository.save(
            User.builder()
                .uid(UUID.randomUUID())
                .email(UUID.randomUUID() + "@test.com")
                .role(Role.ROLE_USER)
                .locked(false)
                .enabled(true)
                .build());
    return jwtUtil.generateToken(user);
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

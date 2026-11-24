package com.acs.bookingsystem.danceclass.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.acs.bookingsystem.BaseIntegrationTest;
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
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
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
class DanceClassAdminControllerIT extends BaseIntegrationTest {

  private static final String URL = "/api/v1/admin/dance-classes";

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private UserRepository userRepository;
  @Autowired private JwtUtil jwtUtil;
  @Autowired private DanceClassRepository danceClassRepository;
  @Autowired private ObjectMapper objectMapper;

  @BeforeEach
  void clearDanceClasses() {
    // Other ITs share this database and leave dance classes behind that would break exact
    // assertions on the list. Rows still referenced by bookings can't be removed, so deactivate.
    danceClassRepository
        .findAllByActiveIsTrue()
        .forEach(
            danceClass -> {
              danceClass.setActive(false);
              danceClassRepository.save(danceClass);
            });
  }

  private String tokenFor(Role role) {
    User user =
        userRepository.save(
            User.builder()
                .uid(UUID.randomUUID())
                .email(UUID.randomUUID() + "@test.com")
                .role(role)
                .locked(false)
                .enabled(true)
                .build());
    return jwtUtil.generateToken(user);
  }

  private ResponseEntity<String> get(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    return restTemplate.exchange(URL, HttpMethod.GET, new HttpEntity<>(headers), String.class);
  }

  @Test
  void givenAdmin_whenGetDanceClasses_thenReturnsOnlyActiveRoomHireRatesInClassTypeOrder()
      throws Exception {
    danceClassRepository.save(
        new DanceClass(ClassType.OTHER, true, new BigDecimal("12.50"), Role.ROLE_ADMIN));
    danceClassRepository.save(
        new DanceClass(ClassType.PRIVATE, true, new BigDecimal("30.00"), Role.ROLE_USER));
    danceClassRepository.save(
        new DanceClass(ClassType.PRACTICE, true, new BigDecimal("8.00"), Role.ROLE_USER));
    danceClassRepository.save(
        new DanceClass(ClassType.GROUP, true, BigDecimal.ZERO, Role.ROLE_ADMIN));
    danceClassRepository.save(
        new DanceClass(ClassType.UNAVAILABLE, true, BigDecimal.ZERO, Role.ROLE_ADMIN));
    danceClassRepository.save(
        new DanceClass(ClassType.PRIVATE, false, new BigDecimal("99.00"), Role.ROLE_USER));

    ResponseEntity<String> response = get(tokenFor(Role.ROLE_ADMIN));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode rates = objectMapper.readTree(response.getBody());
    assertThat(rates).hasSize(3);
    assertThat(rates.get(0).get("classType").asText()).isEqualTo("PRIVATE");
    assertThat(rates.get(0).get("pricePerHour").decimalValue()).isEqualByComparingTo("30.00");
    assertThat(rates.get(0).get("role").asText()).isEqualTo("ROLE_USER");
    assertThat(rates.get(0).get("active").asBoolean()).isTrue();
    assertThat(rates.get(1).get("classType").asText()).isEqualTo("PRACTICE");
    assertThat(rates.get(2).get("classType").asText()).isEqualTo("OTHER");
    assertThat(rates.get(2).get("role").asText()).isEqualTo("ROLE_ADMIN");
  }

  @Test
  void givenAdmin_whenCreateGroupClassRate_thenBadRequestAndNothingSaved() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(tokenFor(Role.ROLE_ADMIN));
    headers.setContentType(MediaType.APPLICATION_JSON);
    String body = "{\"classType\":\"GROUP\",\"pricePerHour\":5.00,\"role\":\"ROLE_USER\"}";

    ResponseEntity<String> response =
        restTemplate.exchange(URL, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(objectMapper.readTree(response.getBody()).get("error").asText())
        .isEqualTo("INVALID_DANCE_CLASS_REQUEST");
    assertThat(danceClassRepository.findByActiveIsTrueAndClassType(ClassType.GROUP)).isEmpty();
  }

  @Test
  void givenAdmin_whenTurnOffClosed_thenBadRequestAndStillActive() {
    danceClassRepository.save(
        new DanceClass(ClassType.UNAVAILABLE, true, BigDecimal.ZERO, Role.ROLE_ADMIN));
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(tokenFor(Role.ROLE_ADMIN));

    ResponseEntity<String> response =
        restTemplate.exchange(
            URL + "/UNAVAILABLE", HttpMethod.DELETE, new HttpEntity<>(headers), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(danceClassRepository.findByActiveIsTrueAndClassType(ClassType.UNAVAILABLE))
        .isPresent();
  }

  @Test
  void givenNonAdmin_whenGetDanceClasses_thenReturnsForbidden() {
    ResponseEntity<String> response = get(tokenFor(Role.ROLE_USER));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }
}

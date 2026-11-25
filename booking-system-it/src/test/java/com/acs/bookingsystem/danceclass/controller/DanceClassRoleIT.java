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
import java.util.ArrayList;
import java.util.List;
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
class DanceClassRoleIT extends BaseIntegrationTest {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private UserRepository userRepository;
  @Autowired private JwtUtil jwtUtil;
  @Autowired private DanceClassRepository danceClassRepository;
  @Autowired private ObjectMapper objectMapper;

  // OTHER and PRACTICE are used only here, so these tests don't disturb other ITs that rely on an
  // active PRIVATE rate.
  @BeforeEach
  void setUpRates() {
    danceClassRepository
        .findByActiveIsTrueAndClassType(ClassType.OTHER)
        .ifPresent(
            d -> {
              d.setActive(false);
              danceClassRepository.save(d);
            });
    danceClassRepository
        .findByActiveIsTrueAndClassType(ClassType.PRACTICE)
        .ifPresent(
            d -> {
              d.setActive(false);
              danceClassRepository.save(d);
            });
    danceClassRepository.save(
        new DanceClass(null, ClassType.OTHER, true, BigDecimal.TEN, Role.ROLE_USER));
    danceClassRepository.save(
        new DanceClass(null, ClassType.PRACTICE, true, BigDecimal.ONE, Role.ROLE_ADMIN));
  }

  private String token(Role role) {
    return jwtUtil.generateToken(
        userRepository.save(
            User.builder()
                .uid(UUID.randomUUID())
                .email(UUID.randomUUID() + "@test.com")
                .role(role)
                .locked(false)
                .enabled(true)
                .build()));
  }

  private HttpHeaders headers(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }

  private ResponseEntity<String> getUrl(String url, String token) {
    return restTemplate.exchange(
        url, HttpMethod.GET, new HttpEntity<>(headers(token)), String.class);
  }

  private ResponseEntity<String> book(String token, ClassType classType, int day) {
    String body =
        String.format(
            "{\"room\":\"ASTAIRE\",\"classType\":\"%s\",\"isShareable\":false,"
                + "\"dateFrom\":\"2033-06-%02dT10:00:00Z\",\"dateTo\":\"2033-06-%02dT11:00:00Z\"}",
            classType, day, day);
    return restTemplate.exchange(
        "/api/v1/bookings", HttpMethod.POST, new HttpEntity<>(body, headers(token)), String.class);
  }

  private List<String> classTypes(String token) throws Exception {
    ResponseEntity<String> response = getUrl("/api/v1/dance-classes/class-types", token);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    List<String> result = new ArrayList<>();
    objectMapper.readTree(response.getBody()).forEach(n -> result.add(n.asText()));
    return result;
  }

  @Test
  void givenAdmin_whenBookUserRate_thenCreated() {
    assertThat(book(token(Role.ROLE_ADMIN), ClassType.OTHER, 1).getStatusCode())
        .isEqualTo(HttpStatus.CREATED);
  }

  @Test
  void givenAdmin_whenBookAdminRate_thenCreated() {
    assertThat(book(token(Role.ROLE_ADMIN), ClassType.PRACTICE, 2).getStatusCode())
        .isEqualTo(HttpStatus.CREATED);
  }

  @Test
  void givenUser_whenBookUserRate_thenCreated() {
    assertThat(book(token(Role.ROLE_USER), ClassType.OTHER, 3).getStatusCode())
        .isEqualTo(HttpStatus.CREATED);
  }

  @Test
  void givenUser_whenBookAdminOnlyRate_thenNotFound() {
    assertThat(book(token(Role.ROLE_USER), ClassType.PRACTICE, 4).getStatusCode())
        .isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void whenGetClassTypes_thenAdminSeesEveryActiveRateAndUserOnlyTheirs() throws Exception {
    assertThat(classTypes(token(Role.ROLE_ADMIN))).contains("OTHER", "PRACTICE");
    List<String> userTypes = classTypes(token(Role.ROLE_USER));
    assertThat(userTypes).contains("OTHER").doesNotContain("PRACTICE");
  }

  @Test
  void whenGetRateByClassType_thenAdminSeesAdminOnlyRateAndUserDoesNot() throws Exception {
    ResponseEntity<String> admin =
        getUrl("/api/v1/dance-classes?classType=PRACTICE", token(Role.ROLE_ADMIN));
    assertThat(admin.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode body = objectMapper.readTree(admin.getBody());
    assertThat(body.get("role").asText()).isEqualTo("ROLE_ADMIN");

    ResponseEntity<String> user =
        getUrl("/api/v1/dance-classes?classType=PRACTICE", token(Role.ROLE_USER));
    assertThat(user.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    ResponseEntity<String> adminUserRate =
        getUrl("/api/v1/dance-classes?classType=OTHER", token(Role.ROLE_ADMIN));
    assertThat(adminUserRate.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}

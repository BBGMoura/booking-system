package com.acs.bookingsystem.user.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.acs.bookingsystem.BaseIntegrationTest;
import com.acs.bookingsystem.user.request.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RateLimitIT extends BaseIntegrationTest {

  @Autowired private TestRestTemplate restTemplate;

  @Test
  void givenMoreThanTenRegistrationsFromSameIp_whenRegister_thenEventuallyReturns429() {
    ResponseEntity<String> lastResponse = null;

    for (int i = 0; i < 11; i++) {
      RegisterRequest request =
          new RegisterRequest(
              "Test", "User", "rate-limit-it-" + i + "@example.com", "07123456789", "Password1!");
      lastResponse = restTemplate.postForEntity("/api/v1/auth/register", request, String.class);
    }

    assertThat(lastResponse.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    assertThat(lastResponse.getHeaders().getFirst("Retry-After")).isNotNull();
  }
}

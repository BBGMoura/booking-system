package com.acs.bookingsystem.security.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

class SecurityConfigCorsTest {

  private static final String ORIGINS =
      " http://localhost:3000 , https://booking-ui-d6mq.onrender.com,,"
          + "https://booking-ui-d6mq-pr-*.onrender.com ";

  private static String resolveAllowedOrigin(String configured, String origin) {
    CorsConfigurationSource source = SecurityConfig.buildCorsConfigurationSource(configured);
    HttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/bookings");
    CorsConfiguration configuration = source.getCorsConfiguration(request);
    return configuration.checkOrigin(origin);
  }

  @Test
  void givenExactOrigin_whenCheckOrigin_thenAllowed() {
    assertThat(resolveAllowedOrigin(ORIGINS, "https://booking-ui-d6mq.onrender.com"))
        .isEqualTo("https://booking-ui-d6mq.onrender.com");
  }

  @Test
  void givenPatternOrigin_whenCheckOrigin_thenAllowed() {
    assertThat(resolveAllowedOrigin(ORIGINS, "https://booking-ui-d6mq-pr-12.onrender.com"))
        .isEqualTo("https://booking-ui-d6mq-pr-12.onrender.com");
  }

  @Test
  void givenSingleOriginConfig_whenCheckOrigin_thenAllowed() {
    assertThat(resolveAllowedOrigin("http://localhost:3000", "http://localhost:3000"))
        .isEqualTo("http://localhost:3000");
  }

  @Test
  void givenNonMatchingOrigin_whenCheckOrigin_thenRejected() {
    assertThat(resolveAllowedOrigin(ORIGINS, "https://evil.example.com")).isNull();
    assertThat(resolveAllowedOrigin(ORIGINS, "https://booking-ui-d6mq-pr-12.evil.com")).isNull();
  }

  @Test
  void givenWhitespaceAndEmptyEntries_whenParse_thenTrimmedAndIgnored() {
    assertThat(SecurityConfig.parseAllowedOrigins(ORIGINS))
        .containsExactly(
            "http://localhost:3000",
            "https://booking-ui-d6mq.onrender.com",
            "https://booking-ui-d6mq-pr-*.onrender.com");
  }

  @Test
  void givenBareWildcard_whenParse_thenFailsFast() {
    assertThatThrownBy(() -> SecurityConfig.parseAllowedOrigins("http://localhost:3000, * "))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("bare '*'");
  }

  @Test
  void givenNoOrigins_whenParse_thenFailsFast() {
    assertThatThrownBy(() -> SecurityConfig.parseAllowedOrigins(" , "))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("at least one origin");
  }
}

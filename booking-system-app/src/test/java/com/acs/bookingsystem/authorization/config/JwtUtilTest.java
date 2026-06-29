package com.acs.bookingsystem.authorization.config;

import static org.junit.jupiter.api.Assertions.*;

import com.acs.bookingsystem.security.model.PasswordResetClaims;
import com.acs.bookingsystem.security.util.JwtUtil;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

  private static final String SECRET_KEY = "Z3C79B4RkvUor/BPyjQOUHHRyxeg5EIT1TtCFK0yNCA=";

  @Mock private UserDetails userDetails;

  private JwtUtil jwtUtil;

  @BeforeEach
  void setUp() throws Exception {
    jwtUtil = new JwtUtil();
    Field field = jwtUtil.getClass().getDeclaredField("secretKey");
    field.setAccessible(true);
    field.set(jwtUtil, JwtUtilTest.SECRET_KEY);
    userDetails = User.withUsername("tester").password("password").authorities("ROLE_USER").build();
  }

  @Test
  void testGenerateToken() {
    String token = jwtUtil.generateToken(userDetails);
    assertFalse(token.isEmpty());
  }

  @Test
  void testExtractUsername() {
    String token = jwtUtil.generateToken(userDetails);
    String username = jwtUtil.extractUsername(token);
    assertEquals("tester", username);
  }

  @Test
  void testIsTokenValid() {
    String token = jwtUtil.generateToken(userDetails);
    assertTrue(jwtUtil.isTokenValid(token, userDetails));
  }

  @Test
  void generatePasswordResetToken_returnsNonEmptyToken() {
    String token =
        jwtUtil.generatePasswordResetToken(
            "user@example.com", "$2a$10$abcdefghijklmnopqrstuvuOPAhbjNTGDwNnYGmkHBDjhBpxQ7yy2");
    assertFalse(token.isEmpty());
  }

  @Test
  void extractPasswordResetClaims_returnsCorrectEmailAndFingerprint() {
    String bcryptHash = "$2a$10$abcdefghijklmnopqrstuvuOPAhbjNTGDwNnYGmkHBDjhBpxQ7yy2";
    String token = jwtUtil.generatePasswordResetToken("user@example.com", bcryptHash);
    PasswordResetClaims claims = jwtUtil.extractPasswordResetClaims(token);
    assertEquals("user@example.com", claims.email());
    assertEquals(bcryptHash, claims.passwordHash());
  }

  @Test
  void extractPasswordResetClaims_throwsForRegularAuthToken() {
    String authToken = jwtUtil.generateToken(userDetails);
    assertThrows(
        com.acs.bookingsystem.common.exception.AuthorizationException.class,
        () -> jwtUtil.extractPasswordResetClaims(authToken));
  }
}

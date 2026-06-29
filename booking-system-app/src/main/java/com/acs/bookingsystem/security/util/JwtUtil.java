package com.acs.bookingsystem.security.util;

import com.acs.bookingsystem.common.exception.AuthorizationException;
import com.acs.bookingsystem.common.exception.model.ErrorCode;
import com.acs.bookingsystem.security.model.PasswordResetClaims;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
  private static final long AUTH_TOKEN_EXPIRY_MS = 1000L * 60 * 60 * 24;
  private static final long PASSWORD_RESET_TOKEN_EXPIRY_MS = 1000L * 60 * 15;
  private static final String CLAIM_TYPE = "type";
  private static final String CLAIM_PASSWORD_HASH = "passwordHash";
  private static final String CLAIM_ROLE = "role";
  private static final String TOKEN_TYPE_PASSWORD_RESET = "password-reset";
  private static final String DEFAULT_ROLE = "ROLE_USER";

  @Value("${jwt.secret.key}")
  private String secretKey;

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  public String generateToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    claims.put(
        CLAIM_ROLE,
        userDetails.getAuthorities().stream()
            .findFirst()
            .map(GrantedAuthority::getAuthority)
            .orElse(DEFAULT_ROLE));
    return generateToken(claims, userDetails);
  }

  public String generateToken(Map<String, Object> extractClaims, UserDetails userDetails) {
    try {
      return Jwts.builder()
          .claims(extractClaims)
          .subject(userDetails.getUsername())
          .issuedAt(new Date(System.currentTimeMillis()))
          .expiration(new Date(System.currentTimeMillis() + AUTH_TOKEN_EXPIRY_MS))
          .signWith(getSigningKey())
          .compact();
    } catch (JwtException jwtE) {
      throw new AuthorizationException("Error generating token", jwtE, ErrorCode.INVALID_TOKEN);
    }
  }

  public String generatePasswordResetToken(String email, String passwordHash) {
    try {
      return Jwts.builder()
          .claim(CLAIM_TYPE, TOKEN_TYPE_PASSWORD_RESET)
          .claim(CLAIM_PASSWORD_HASH, passwordHash)
          .subject(email)
          .issuedAt(new Date(System.currentTimeMillis()))
          .expiration(new Date(System.currentTimeMillis() + PASSWORD_RESET_TOKEN_EXPIRY_MS))
          .signWith(getSigningKey())
          .compact();
    } catch (JwtException jwtE) {
      throw new AuthorizationException(
          "Error generating password reset token", jwtE, ErrorCode.INVALID_TOKEN);
    }
  }

  public PasswordResetClaims extractPasswordResetClaims(String token) {
    Claims claims = extractAllClaims(token);
    if (!TOKEN_TYPE_PASSWORD_RESET.equals(claims.get(CLAIM_TYPE, String.class))) {
      throw new AuthorizationException("Not a password reset token", null, ErrorCode.INVALID_TOKEN);
    }
    return new PasswordResetClaims(
        claims.getSubject(), claims.get(CLAIM_PASSWORD_HASH, String.class));
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
  }

  protected boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  private Claims extractAllClaims(String token) {
    try {
      return Jwts.parser()
          .verifyWith(getSigningKey())
          .build()
          .parseSignedClaims(token)
          .getPayload();
    } catch (JwtException jwtE) {
      throw new AuthorizationException("Error parsing token claims", jwtE, ErrorCode.INVALID_TOKEN);
    }
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  private SecretKey getSigningKey() {
    try {
      byte[] bytes = Base64.getDecoder().decode(secretKey.getBytes(StandardCharsets.UTF_8));
      return Keys.hmacShaKeyFor(bytes);
    } catch (JwtException jwtE) {
      throw new AuthorizationException("Error decoding signing key", jwtE, ErrorCode.INVALID_TOKEN);
    }
  }
}

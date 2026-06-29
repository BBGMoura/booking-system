package com.acs.bookingsystem.security.util;

import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PasswordUtil {

  private final PasswordEncoder passwordEncoder;

  public String encodePassword(String rawPassword) {
    return passwordEncoder.encode(rawPassword);
  }
}

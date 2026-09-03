package com.acs.bookingsystem.security.config;

import com.acs.bookingsystem.security.util.PasswordUtil;
import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.enums.Role;
import com.acs.bookingsystem.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Creates the first admin user on startup if none exists yet, from {@code
 * ADMIN_BOOTSTRAP_EMAIL}/{@code ADMIN_BOOTSTRAP_PASSWORD}. Never runs again once an admin exists —
 * further password changes go through the normal reset/change-password flow, not this runner.
 */
@Component
@Slf4j
public class AdminBootstrapRunner implements ApplicationRunner {

  private final UserRepository userRepository;
  private final PasswordUtil passwordUtil;
  private final String adminEmail;
  private final String adminPassword;

  public AdminBootstrapRunner(
      UserRepository userRepository,
      PasswordUtil passwordUtil,
      @Value("${admin.bootstrap.email}") String adminEmail,
      @Value("${admin.bootstrap.password}") String adminPassword) {
    this.userRepository = userRepository;
    this.passwordUtil = passwordUtil;
    this.adminEmail = adminEmail;
    this.adminPassword = adminPassword;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (userRepository.existsByRole(Role.ROLE_ADMIN)) {
      return;
    }

    if (adminEmail.isBlank() || adminPassword.isBlank()) {
      log.warn(
          "No admin user exists and ADMIN_BOOTSTRAP_EMAIL/ADMIN_BOOTSTRAP_PASSWORD are not set."
              + " The app has no usable admin account.");
      return;
    }

    userRepository.save(
        User.builder()
            .email(adminEmail)
            .password(passwordUtil.encodePassword(adminPassword))
            .role(Role.ROLE_ADMIN)
            .locked(false)
            .enabled(true)
            .build());
  }
}

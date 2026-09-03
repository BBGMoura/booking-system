package com.acs.bookingsystem.security.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.acs.bookingsystem.security.util.PasswordUtil;
import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.enums.Role;
import com.acs.bookingsystem.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapRunnerTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordUtil passwordUtil;
  @Mock private ApplicationArguments applicationArguments;

  @Test
  void givenAdminAlreadyExists_whenRun_thenDoesNotCreateUser() throws Exception {
    when(userRepository.existsByRole(Role.ROLE_ADMIN)).thenReturn(true);
    AdminBootstrapRunner runner =
        new AdminBootstrapRunner(userRepository, passwordUtil, "admin@example.com", "Password1!");

    runner.run(applicationArguments);

    verify(userRepository, never()).save(any());
  }

  @Test
  void givenNoAdminAndConfigPresent_whenRun_thenCreatesEnabledAdminWithEncodedPassword()
      throws Exception {
    when(userRepository.existsByRole(Role.ROLE_ADMIN)).thenReturn(false);
    when(passwordUtil.encodePassword("Password1!")).thenReturn("encoded");
    AdminBootstrapRunner runner =
        new AdminBootstrapRunner(userRepository, passwordUtil, "admin@example.com", "Password1!");

    runner.run(applicationArguments);

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    User saved = captor.getValue();
    assertThat(saved.getEmail()).isEqualTo("admin@example.com");
    assertThat(saved.getPassword()).isEqualTo("encoded");
    assertThat(saved.getRole()).isEqualTo(Role.ROLE_ADMIN);
    assertThat(saved.isEnabled()).isTrue();
    assertThat(saved.isLocked()).isFalse();
  }

  @Test
  void givenNoAdminAndConfigBlank_whenRun_thenDoesNotCreateUser() throws Exception {
    when(userRepository.existsByRole(Role.ROLE_ADMIN)).thenReturn(false);
    AdminBootstrapRunner runner = new AdminBootstrapRunner(userRepository, passwordUtil, "", "");

    runner.run(applicationArguments);

    verify(userRepository, never()).save(any());
  }
}

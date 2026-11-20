package com.acs.bookingsystem.danceclass.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.acs.bookingsystem.common.exception.NotFoundException;
import com.acs.bookingsystem.danceclass.DanceClassTestData;
import com.acs.bookingsystem.danceclass.entity.DanceClass;
import com.acs.bookingsystem.danceclass.enums.ClassType;
import com.acs.bookingsystem.danceclass.repository.DanceClassRepository;
import com.acs.bookingsystem.danceclass.request.DanceClassRequest;
import com.acs.bookingsystem.user.UserTestData;
import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.enums.Role;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class DanceClassServiceTest {

  @Mock private DanceClassRepository danceClassRepository;

  @InjectMocks private DanceClassService danceClassService;

  private final DanceClass danceClass = DanceClassTestData.danceClass;
  private final DanceClassRequest danceClassRequest = DanceClassTestData.danceClassRequest;
  private final User user = UserTestData.user;

  @BeforeEach
  void setup() {
    Authentication auth =
        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  void whenUserAdmin_createDanceClass_danceClassReturned() {
    when(danceClassRepository.findByActiveIsTrueAndClassType(any()))
        .thenReturn(Optional.ofNullable(danceClass));
    when(danceClassRepository.save(any())).thenReturn(danceClass);

    final DanceClass actual = danceClassService.createDanceClass(danceClassRequest);

    assertEquals(danceClass, actual);
  }

  @Test
  void givenAdmin_getActiveDanceClass_returnsUserRateIgnoringRole() {
    when(danceClassRepository.findByActiveIsTrueAndClassType(ClassType.GROUP))
        .thenReturn(Optional.of(danceClass));

    assertEquals(
        danceClass, danceClassService.getActiveDanceClass(ClassType.GROUP, Role.ROLE_ADMIN));
    verify(danceClassRepository, never()).findByActiveIsTrueAndClassTypeAndRole(any(), any());
  }

  @Test
  void givenAdmin_getActiveDanceClass_returnsAdminRate() {
    DanceClass adminRate =
        new DanceClass(2L, ClassType.PRACTICE, true, BigDecimal.ONE, Role.ROLE_ADMIN);
    when(danceClassRepository.findByActiveIsTrueAndClassType(ClassType.PRACTICE))
        .thenReturn(Optional.of(adminRate));

    assertEquals(
        adminRate, danceClassService.getActiveDanceClass(ClassType.PRACTICE, Role.ROLE_ADMIN));
  }

  @Test
  void givenAdminAndNoActiveRate_getActiveDanceClass_throwsNotFound() {
    when(danceClassRepository.findByActiveIsTrueAndClassType(ClassType.OTHER))
        .thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class,
        () -> danceClassService.getActiveDanceClass(ClassType.OTHER, Role.ROLE_ADMIN));
  }

  @Test
  void givenUserAndAdminOnlyRate_getActiveDanceClass_throwsNotFound() {
    when(danceClassRepository.findByActiveIsTrueAndClassTypeAndRole(
            ClassType.PRACTICE, Role.ROLE_USER))
        .thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class,
        () -> danceClassService.getActiveDanceClass(ClassType.PRACTICE, Role.ROLE_USER));
    verify(danceClassRepository, never()).findByActiveIsTrueAndClassType(any());
  }

  @Test
  void givenAdmin_getAllActiveDanceClassTypes_returnsEveryActiveRate() {
    when(danceClassRepository.findAllByActiveIsTrue())
        .thenReturn(
            List.of(
                danceClass,
                new DanceClass(2L, ClassType.PRACTICE, true, BigDecimal.ONE, Role.ROLE_ADMIN)));

    assertEquals(
        List.of(ClassType.GROUP, ClassType.PRACTICE),
        danceClassService.getAllActiveDanceClassTypes(Role.ROLE_ADMIN));
  }

  @Test
  void givenUser_getAllActiveDanceClassTypes_returnsOnlyUserRates() {
    when(danceClassRepository.findAllByActiveIsTrueAndRole(Role.ROLE_USER))
        .thenReturn(List.of(danceClass));

    assertEquals(
        List.of(ClassType.GROUP), danceClassService.getAllActiveDanceClassTypes(Role.ROLE_USER));
    verify(danceClassRepository, never()).findAllByActiveIsTrue();
  }
}

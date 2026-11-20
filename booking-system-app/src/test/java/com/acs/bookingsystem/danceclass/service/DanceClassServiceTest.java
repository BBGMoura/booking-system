package com.acs.bookingsystem.danceclass.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
  void whenGetAllActiveDanceClasses_thenSortedByClassTypeEnumOrder() {
    final DanceClass other =
        new DanceClass(3L, ClassType.OTHER, true, BigDecimal.ONE, Role.ROLE_ADMIN);
    final DanceClass practice =
        new DanceClass(2L, ClassType.PRACTICE, true, BigDecimal.TEN, Role.ROLE_USER);
    final DanceClass privateLesson =
        new DanceClass(1L, ClassType.PRIVATE, true, BigDecimal.TEN, Role.ROLE_USER);
    when(danceClassRepository.findAllByActiveIsTrue())
        .thenReturn(List.of(other, practice, privateLesson));

    final List<DanceClass> actual = danceClassService.getAllActiveDanceClasses();

    assertEquals(List.of(privateLesson, practice, other), actual);
  }

  @Test
  void whenUserAdmin_createDanceClass_danceClassReturned() {
    when(danceClassRepository.findByActiveIsTrueAndClassType(any()))
        .thenReturn(Optional.ofNullable(danceClass));
    when(danceClassRepository.save(any())).thenReturn(danceClass);

    final DanceClass actual = danceClassService.createDanceClass(danceClassRequest);

    assertEquals(danceClass, actual);
  }
}

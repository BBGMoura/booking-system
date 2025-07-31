package com.acs.bookingsystem.danceclass.service;

import com.acs.bookingsystem.danceclass.DanceClassTestData;
import com.acs.bookingsystem.danceclass.entity.DanceClass;
import com.acs.bookingsystem.danceclass.mapper.DanceClassMapper;
import com.acs.bookingsystem.danceclass.repository.DanceClassRepository;
import com.acs.bookingsystem.danceclass.request.DanceClassRequest;
import com.acs.bookingsystem.danceclass.service.impl.DanceClassService;
import com.acs.bookingsystem.user.UserTestData;
import com.acs.bookingsystem.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DanceClassServiceTest {

    @Mock
    private DanceClassRepository danceClassRepository;

    @Mock
    private DanceClassMapper danceClassMapper;

    @InjectMocks
    private DanceClassService danceClassService;

    private final DanceClass danceClass = DanceClassTestData.danceClass;
    private final DanceClassRequest danceClassRequest = DanceClassTestData.danceClassRequest;
    private final User user = UserTestData.user;

    @BeforeEach
    void setup() {
        Authentication auth = new UsernamePasswordAuthenticationToken(user,
                                                                      null,
                                                                      user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void whenUserAdmin_createDanceClass_danceClassDTOReturned() {
        when(danceClassRepository.findByActiveIsTrueAndClassType(any())).thenReturn(Optional.ofNullable(danceClass));
        when(danceClassRepository.save(any())).thenReturn(danceClass);

        final DanceClass actual = danceClassService.createDanceClass(danceClassRequest);

        assertEquals(danceClass, actual);
    }
}
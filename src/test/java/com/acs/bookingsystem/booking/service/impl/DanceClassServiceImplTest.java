package com.acs.bookingsystem.booking.service.impl;

import com.acs.bookingsystem.booking.dto.DanceClassDTO;
import com.acs.bookingsystem.booking.entities.DanceClass;
import com.acs.bookingsystem.booking.enums.ClassType;
import com.acs.bookingsystem.common.exception.NotFoundException;
import com.acs.bookingsystem.booking.mapper.DanceClassMapper;
import com.acs.bookingsystem.booking.repository.DanceClassRepository;
import com.acs.bookingsystem.booking.request.DanceClassRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.acs.bookingsystem.TestDataUtil.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DanceClassServiceImplTest {
    @Mock
    private DanceClassRepository danceClassRepository;
    @Mock
    private DanceClassMapper danceClassMapper;
    @InjectMocks
    private DanceClassServiceImpl danceClassService;

    @Test
    void createDanceClass_resultsSuccessful() {
        //given
        DanceClassRequest request = createDanceClassRequest(ClassType.GROUP);
        DanceClass danceClass = createDanceClass(ClassType.GROUP,true);
        DanceClassDTO expectedDto = createDanceClassDTO(ClassType.GROUP,true);

        when(danceClassRepository.save(any(DanceClass.class))).thenReturn(danceClass);
        when(danceClassMapper.mapDanceClassToDTO(danceClass)).thenReturn(expectedDto);

        // When
        DanceClassDTO actualDto = danceClassService.createDanceClass(request);

        // Then
        assertNotNull(actualDto);
        assertEquals(expectedDto, actualDto);
        verify(danceClassRepository, times(1)).save(any(DanceClass.class));
        verify(danceClassMapper, times(1)).mapDanceClassToDTO(danceClass);
    }



    @Test
    void testCreateDanceClassWithExistingActiveDanceClass() {
        // Given
        DanceClassRequest request = createDanceClassRequest(ClassType.GROUP);
        DanceClass existingDanceClass = createDanceClass(ClassType.GROUP,true);
        DanceClass newDanceClass = createDanceClass(ClassType.GROUP,true);
        DanceClassDTO expectedDto = createDanceClassDTO(ClassType.GROUP,true);

        when(danceClassRepository.findByActiveIsTrueAndClassType(ClassType.GROUP)).thenReturn(Optional.of(existingDanceClass));
        when(danceClassRepository.save(any(DanceClass.class))).thenReturn(newDanceClass);
        when(danceClassMapper.mapDanceClassToDTO(newDanceClass)).thenReturn(expectedDto);

        // When
        DanceClassDTO actualDto = danceClassService.createDanceClass(request);

        // Then
        verify(danceClassRepository, times(1)).findByActiveIsTrueAndClassType(ClassType.GROUP);
        verify(danceClassRepository, times(2)).save(any(DanceClass.class));
        verify(danceClassMapper, times(1)).mapDanceClassToDTO(newDanceClass);
        assertFalse(existingDanceClass.isActive());
        assertEquals(expectedDto, actualDto);
    }

    @Test
    void getAllActiveDanceClassTypes() {
        // Given
        List<ClassType> expectedClassTypes = Collections.singletonList(ClassType.GROUP);
        DanceClass danceClass = createDanceClass(ClassType.GROUP,true);

        when(danceClassRepository.findAllByActiveIsTrue()).thenReturn(Collections.singletonList(danceClass));

        // When
        List<ClassType> actualClassTypes = danceClassService.getAllActiveDanceClassTypes();

        // Then
        assertNotNull(actualClassTypes);
        assertEquals(expectedClassTypes, actualClassTypes);
        verify(danceClassRepository, times(1)).findAllByActiveIsTrue();
    }

    @Test
    void testGetDanceClassByActiveClassType() {
        // Given
        DanceClass danceClass = createDanceClass(ClassType.GROUP,true);
        DanceClassDTO expectedDto = createDanceClassDTO(ClassType.GROUP,true);

        when(danceClassRepository.findByActiveIsTrueAndClassType(ClassType.GROUP)).thenReturn(Optional.of(danceClass));
        when(danceClassMapper.mapDanceClassToDTO(danceClass)).thenReturn(expectedDto);

        // When
        DanceClassDTO actualDto = danceClassService.getDanceClassByActiveClassType(ClassType.GROUP);

        // Then
        assertNotNull(actualDto);
        assertEquals(expectedDto, actualDto);
        verify(danceClassRepository, times(1)).findByActiveIsTrueAndClassType(ClassType.GROUP);
        verify(danceClassMapper, times(1)).mapDanceClassToDTO(danceClass);
    }

    @Test
    void testDeactivateDanceClass() {
        // Given
        DanceClass danceClass = createDanceClass(ClassType.GROUP,true);
        when(danceClassRepository.findByActiveIsTrueAndClassType(ClassType.GROUP)).thenReturn(Optional.of(danceClass));

        // When
        danceClassService.deactivateDanceClass(ClassType.GROUP);

        // Then
        assertFalse(danceClass.isActive());
        verify(danceClassRepository, times(1)).findByActiveIsTrueAndClassType(ClassType.GROUP);
        verify(danceClassRepository, times(1)).save(danceClass);
    }

    @Test
    void
    testDeactivateDanceClass_NotFound() {
        // Given
        when(danceClassRepository.findByActiveIsTrueAndClassType(ClassType.GROUP)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(NotFoundException.class, () -> danceClassService.deactivateDanceClass(ClassType.GROUP));
        verify(danceClassRepository, times(1)).findByActiveIsTrueAndClassType(ClassType.GROUP);
        verify(danceClassRepository, never()).save(any(DanceClass.class));
    }

}

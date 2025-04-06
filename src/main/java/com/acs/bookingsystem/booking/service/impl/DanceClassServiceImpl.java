package com.acs.bookingsystem.booking.service.impl;

import com.acs.bookingsystem.booking.dto.DanceClassDTO;
import com.acs.bookingsystem.booking.entities.DanceClass;
import com.acs.bookingsystem.booking.enums.ClassType;
import com.acs.bookingsystem.booking.service.DanceClassService;
import com.acs.bookingsystem.common.exception.NotFoundException;
import com.acs.bookingsystem.booking.mapper.DanceClassMapper;
import com.acs.bookingsystem.booking.request.DanceClassRequest;
import com.acs.bookingsystem.booking.repository.DanceClassRepository;
import com.acs.bookingsystem.common.exception.model.ErrorCode;
import com.acs.bookingsystem.user.enums.Role;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class DanceClassServiceImpl implements DanceClassService {
    private DanceClassRepository danceClassRepository;
    private DanceClassMapper danceClassMapper;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public DanceClassDTO createDanceClass(DanceClassRequest danceClassRequest) {
        danceClassRepository.findByActiveIsTrueAndClassType(danceClassRequest.classType())
                            .ifPresent(this::deactivateDanceClass);

        final DanceClass danceClass = new DanceClass(danceClassRequest.classType(),
                                                     true,
                                                     danceClassRequest.pricePerHour(),
                                                     danceClassRequest.role());

        return danceClassMapper.mapDanceClassToDTO(danceClassRepository.save(danceClass));
    }

    public List<ClassType> getAllActiveDanceClassTypes(Role role) {
        List<DanceClass> danceClasses = danceClassRepository.findAllByActiveIsTrueAndRole(role);
        return danceClasses.stream()
                           .map(DanceClass::getClassType)
                           .toList();
    }

    public DanceClassDTO getActiveDanceClass(ClassType classType, Role role) {
        final DanceClass danceClass = danceClassRepository.findByActiveIsTrueAndClassTypeAndRole(classType, role)
                                                          .orElseThrow(() -> new NotFoundException("Dance class with type " + classType + "has not been found. Please contact support.",
                                                                                                    ErrorCode.INVALID_DANCE_CLASS_REQUEST));
        return danceClassMapper.mapDanceClassToDTO(danceClass);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deactivateDanceClassByClassType(ClassType classType) {
        final DanceClass danceClass = danceClassRepository.findByActiveIsTrueAndClassType(classType)
                                                          .orElseThrow(() -> new NotFoundException("The dance class with type "+classType+" has not been recognised. Please contact support.",
                                                                                                    ErrorCode.INVALID_DANCE_CLASS_REQUEST));

        deactivateDanceClass(danceClass);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    private void deactivateDanceClass(DanceClass danceClass) {
            danceClass.setActive(false);
            danceClassRepository.save(danceClass);
    }
}
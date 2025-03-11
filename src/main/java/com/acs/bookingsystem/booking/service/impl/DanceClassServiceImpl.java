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
import lombok.AllArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class DanceClassServiceImpl implements DanceClassService {
    DanceClassRepository danceClassRepository;
    DanceClassMapper danceClassMapper;

    @Secured("ADMIN")
    public DanceClassDTO createDanceClass(DanceClassRequest danceClassRequest) {
        danceClassRepository.findByActiveIsTrueAndClassType(danceClassRequest.getClassType())
                            .ifPresent(this::deactivateDanceClass);

//        validatePrices(danceClassRequest);

        final DanceClass danceClass = new DanceClass(danceClassRequest.getClassType(),
                                                     true,
                                                     danceClassRequest.getPricePerHour());

        return danceClassMapper.mapDanceClassToDTO(danceClassRepository.save(danceClass));
    }

    public List<ClassType> getAllActiveDanceClassTypes() {
        // TODO: certain class types are restricted to only admins being able to book
        List<DanceClass> danceClasses = danceClassRepository.findAllByActiveIsTrue();
        return danceClasses.stream()
                           .map(DanceClass::getClassType)
                           .toList();
    }

    public DanceClassDTO getActiveDanceClassByClassType(ClassType classType) {
        return danceClassMapper.mapDanceClassToDTO(getActiveDanceClassByType(classType));
    }

    public void deactivateDanceClassByClassType(ClassType classType) {
        deactivateDanceClass(getActiveDanceClassByType(classType));
    }

    private DanceClass getActiveDanceClassByType(ClassType classType) {
        return danceClassRepository.findByActiveIsTrueAndClassType(classType)
                                   .orElseThrow(() -> new NotFoundException("The dance class "+classType+" has not been recognised. Please contact support.", ErrorCode.INVALID_DANCE_CLASS_REQUEST));
    }

    private void deactivateDanceClass(DanceClass danceClass) {
            danceClass.setActive(false);
            danceClassRepository.save(danceClass);
    }

    // TODO: VALIDATE PRICES

//    private void validatePrices(DanceClassRequest danceClassRequest){
//        final boolean allZero = danceClassRequest.getPricePerHour().compareTo(BigDecimal.ZERO) == 0 &&
//                                danceClassRequest.getPricePer45().compareTo(BigDecimal.ZERO) == 0 &&
//                                danceClassRequest.getPricePer30().compareTo(BigDecimal.ZERO) == 0;
//
//        final boolean allGreaterThanZero = danceClassRequest.getPricePerHour().compareTo(BigDecimal.ZERO) > 0 &&
//                                           danceClassRequest.getPricePer45().compareTo(BigDecimal.ZERO) > 0 &&
//                                           danceClassRequest.getPricePer30().compareTo(BigDecimal.ZERO) > 0;
//
//        if (!allZero && !allGreaterThanZero) {
//            throw new RequestException("Prices for dance class must be all be 0 or all greater than 0.", ErrorCode.INVALID_DANCE_CLASS_REQUEST);
//        }
//    }
}

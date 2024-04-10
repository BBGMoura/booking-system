package com.acs.bookingsystem.booking.service.impl;

import com.acs.bookingsystem.booking.dto.DanceClassDTO;
import com.acs.bookingsystem.booking.entities.DanceClass;
import com.acs.bookingsystem.booking.enums.ClassType;
import com.acs.bookingsystem.booking.exception.DanceClassNotFoundException;
import com.acs.bookingsystem.booking.mapper.DanceClassMapper;
import com.acs.bookingsystem.booking.service.DanceClassService;
import com.acs.bookingsystem.booking.repository.DanceClassRepository;
import com.acs.bookingsystem.common.exception.ErrorCode;
import com.acs.bookingsystem.common.exception.RequestException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class DanceClassServiceImpl implements DanceClassService {
    DanceClassRepository danceClassRepository;
    DanceClassMapper danceClassMapper;

    @Override
    public DanceClassDTO createDanceClass(DanceClass danceClass) {
        deactivateDanceClassType(danceClass.getClassType());
        return danceClassMapper.mapDanceClassToDTO(danceClassRepository.save(danceClass));
    }

    @Override
    public List<ClassType> getAllDanceClassTypes() {
        //add based on permission feature
        List<DanceClass> danceClasses = (List<DanceClass>) danceClassRepository.findAllByActiveIsTrue();
        return danceClasses.stream()
                           .map(DanceClass::getClassType)
                           .toList();
    }

    @Override
    public DanceClassDTO getDanceClassByActiveClassType(ClassType classType) {
        return danceClassMapper.mapDanceClassToDTO(getActiveDanceClassByType(classType));
    }

    @Override
    public DanceClassDTO getDanceClassById(int id) {
        return danceClassMapper.mapDanceClassToDTO(findDanceClassById(id));
    }

    @Override
    public void deactivateDanceClass(DanceClass danceClass) {
        deactivateDanceClassType(danceClass.getClassType());
    }

    private DanceClass findDanceClassById(int id){
        return danceClassRepository.findById(id)
                                   .orElseThrow(() -> new RequestException(String.format("DanceClass with ID: %d does not exist", id), ErrorCode.INVALID_ID));
    }

    private DanceClass getActiveDanceClassByType(ClassType classType) {
        return danceClassRepository.findByActiveIsTrueAndClassType(classType)
                                   .orElseThrow(() -> new DanceClassNotFoundException(String.format("The dance class %s has not been recognised. Please contact support.", classType), ErrorCode.INVALID_BOOKING_REQUEST));
    }

    private void deactivateDanceClassType(ClassType classType) {
        Optional<DanceClass> optionalClass = danceClassRepository.findByActiveIsTrueAndClassType(classType);
        if (optionalClass.isPresent()) {
            DanceClass currentClass = optionalClass.get();
            currentClass.setActive(false);
            danceClassRepository.save(currentClass);
        }
    }
}

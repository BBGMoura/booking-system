package com.acs.bookingsystem.booking.service;

import com.acs.bookingsystem.booking.dto.DanceClassDTO;
import com.acs.bookingsystem.booking.enums.ClassType;
import com.acs.bookingsystem.booking.request.DanceClassRequest;

import java.util.List;

public interface DanceClassService {
    DanceClassDTO createDanceClass(DanceClassRequest danceClassRequest);
    List<ClassType> getAllActiveDanceClassTypes();
    DanceClassDTO getActiveDanceClassByClassType(ClassType classType);
    void deactivateDanceClassByClassType(ClassType classType);
}

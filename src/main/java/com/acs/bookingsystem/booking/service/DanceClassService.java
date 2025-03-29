package com.acs.bookingsystem.booking.service;

import com.acs.bookingsystem.booking.dto.DanceClassDTO;
import com.acs.bookingsystem.booking.enums.ClassType;
import com.acs.bookingsystem.booking.request.DanceClassRequest;
import com.acs.bookingsystem.user.enums.Role;

import java.util.List;

public interface DanceClassService {
    DanceClassDTO createDanceClass(DanceClassRequest danceClassRequest);
    List<ClassType> getAllActiveDanceClassTypes(Role role);
    DanceClassDTO getActiveDanceClass(ClassType classType, Role role);
    void deactivateDanceClassByClassType(ClassType classType);
}

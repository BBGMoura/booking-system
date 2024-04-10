package com.acs.bookingsystem.booking.service;

import com.acs.bookingsystem.booking.dto.DanceClassDTO;
import com.acs.bookingsystem.booking.entities.DanceClass;
import com.acs.bookingsystem.booking.enums.ClassType;

import java.util.List;

public interface DanceClassService {
    DanceClassDTO createDanceClass(DanceClass danceClass);
    List<ClassType> getAllDanceClassTypes();
    DanceClassDTO getDanceClassByActiveClassType(ClassType classType);
    DanceClassDTO getDanceClassById(int id);
    void deactivateDanceClass(DanceClass danceClass);
}

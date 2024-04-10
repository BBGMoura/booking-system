package com.acs.bookingsystem.booking.repository;

import com.acs.bookingsystem.booking.entities.DanceClass;
import com.acs.bookingsystem.booking.enums.ClassType;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface DanceClassRepository extends CrudRepository<DanceClass, Integer> {
    Optional<DanceClass> findByActiveIsTrueAndClassType(ClassType classType);
    List<DanceClass> findAllByActiveIsTrue();

}

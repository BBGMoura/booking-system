package com.acs.bookingsystem.danceclass.repository;

import com.acs.bookingsystem.danceclass.entity.DanceClass;
import com.acs.bookingsystem.danceclass.enums.ClassType;
import com.acs.bookingsystem.user.enums.Role;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface DanceClassRepository extends CrudRepository<DanceClass, Integer> {
    Optional<DanceClass> findByActiveIsTrueAndClassType(ClassType classType);
    Optional<DanceClass> findByActiveIsTrueAndClassTypeAndRole(ClassType classType, Role role);
    List<DanceClass> findAllByActiveIsTrueAndRole(Role role);

}

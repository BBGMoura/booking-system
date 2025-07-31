package com.acs.bookingsystem.danceclass.mapper;

import com.acs.bookingsystem.danceclass.dto.DanceClassDTO;
import com.acs.bookingsystem.danceclass.entity.DanceClass;
import org.springframework.stereotype.Component;

@Component
public class DanceClassMapper {
    public DanceClassDTO map(DanceClass danceClass) {
        return new DanceClassDTO(danceClass.getId(),
                                 danceClass.getClassType(),
                                 danceClass.isActive(),
                                 danceClass.getPricePerHour(),
                                 danceClass.getRole());
    }
}

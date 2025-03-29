package com.acs.bookingsystem.booking.mapper;

import com.acs.bookingsystem.booking.dto.DanceClassDTO;
import com.acs.bookingsystem.booking.entities.DanceClass;
import org.springframework.stereotype.Service;

@Service
public class DanceClassMapper {
    public DanceClassDTO mapDanceClassToDTO(DanceClass danceClass) {
        return new DanceClassDTO(danceClass.getId(),
                                 danceClass.getClassType(),
                                 danceClass.isActive(),
                                 danceClass.getPricePerHour(),
                                 danceClass.getRole());
    }

    public DanceClass mapDtoToDanceClass(DanceClassDTO danceClassDTO){
        return new DanceClass(danceClassDTO.id(),
                              danceClassDTO.classType(),
                              danceClassDTO.active(),
                              danceClassDTO.pricePerHour(),
                              danceClassDTO.role());
    }
}

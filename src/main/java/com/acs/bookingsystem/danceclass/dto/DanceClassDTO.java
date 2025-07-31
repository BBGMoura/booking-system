package com.acs.bookingsystem.danceclass.dto;

import com.acs.bookingsystem.danceclass.enums.ClassType;
import com.acs.bookingsystem.user.enums.Role;

import java.math.BigDecimal;

public record DanceClassDTO(
        int id,
        ClassType classType,
        boolean active,
        BigDecimal pricePerHour,
        Role role
) {
}

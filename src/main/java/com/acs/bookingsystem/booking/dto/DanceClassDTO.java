package com.acs.bookingsystem.booking.dto;

import com.acs.bookingsystem.booking.enums.ClassType;
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

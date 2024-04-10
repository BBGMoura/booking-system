package com.acs.bookingsystem.booking.dto;

import com.acs.bookingsystem.booking.enums.ClassType;

import java.math.BigDecimal;

public record DanceClassDTO(
        int id,
        ClassType classType,
        boolean active,
        BigDecimal pricePer60,
        BigDecimal pricePer45,
        BigDecimal pricePer30
) {
}

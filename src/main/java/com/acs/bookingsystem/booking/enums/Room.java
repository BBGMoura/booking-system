package com.acs.bookingsystem.booking.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Room {
    ASTAIRE("ASTA"),
    BUSSELL("BUSS"),
    ALEX_MOORE("ALEX"),
    FOSSE("FOSS");

    private final String code;
}

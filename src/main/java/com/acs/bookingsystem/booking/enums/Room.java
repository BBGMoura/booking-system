package com.acs.bookingsystem.booking.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Room {
    ASTAIRE("1"),
    BUSSELL("2"),
    ALEX_MOORE("3"),
    FOSSE("4");

    private final String code;
}

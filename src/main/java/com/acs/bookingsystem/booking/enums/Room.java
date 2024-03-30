package com.acs.bookingsystem.booking.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Room {
    ROOM1("1"),
    ROOM2("2"),
    ROOM3("3"),
    ROOM4("4");

    private final String code;
}

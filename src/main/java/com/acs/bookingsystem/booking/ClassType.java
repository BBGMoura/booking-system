package com.acs.bookingsystem.booking;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ClassType {
    PRIVATE("PRIV"),
    PRACTICE("PRA"),
    GROUP("GRP"),
    OTHER("OTH");

    private final String code;
}

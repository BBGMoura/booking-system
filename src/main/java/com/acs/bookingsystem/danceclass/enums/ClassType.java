package com.acs.bookingsystem.danceclass.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ClassType {
    PRIVATE("PRIV"),
    PRACTICE("PRA"),
    GROUP("GRP"),
    UNAVAILABLE("UN"),
    OTHER("OTH");

    private final String code;
}

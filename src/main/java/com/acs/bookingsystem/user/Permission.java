package com.acs.bookingsystem.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Permission {
    ADMIN("A"),
    USER("U");

    private final String code;
}

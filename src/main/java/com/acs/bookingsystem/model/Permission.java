package com.acs.bookingsystem.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Permission {
    ADMIN("A"),
    USER("U");

    private final String code;
}

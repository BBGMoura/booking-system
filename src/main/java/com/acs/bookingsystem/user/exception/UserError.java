package com.acs.bookingsystem.user.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserError {
    INVALID_ID( "ID is Invalid"),
    EMAIL_ALREADY_EXISTS( "User with email already exists");

    private final String description;
}

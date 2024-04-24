package com.acs.bookingsystem.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INVALID_ID( "ID is Invalid"),
    EMAIL_ALREADY_EXISTS( "User with email already exists"),
    INVALID_BOOKING_REQUEST("Booking request is invalid."),
    INTERNAL_BOOKING_ERROR("Internal Booking Error. Please Contact support."),
    INVALID_DANCE_CLASS_REQUEST("Dance Class request is invalid.");

    private final String description;
}

package com.acs.bookingsystem.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INVALID_USER_ID( "User ID is Invalid"),
    INVALID_BOOKING_ID("Booking ID is inactive."),
    EMAIL_ALREADY_EXISTS( "User with email already exists"),
    INVALID_BOOKING_REQUEST("Booking request is invalid."),
    INTERNAL_BOOKING_ERROR("Internal Booking Error. Please Contact support."),
    INVALID_DANCE_CLASS_REQUEST("Dance Class request is invalid."),
    INTERNAL_ERROR("Internal Error. Please contact support."),
    INACTIVE_USER("User is is not active.");

    private final String description;
}

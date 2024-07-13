package com.acs.bookingsystem.common.exception.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INVALID_USER_ID( "Cannot find user."),
    INVALID_BOOKING_ID("Booking ID is invalid."),
    EMAIL_ALREADY_EXISTS( "User with email already exists."),
    INVALID_BOOKING_REQUEST("Booking request is invalid."),
    INTERNAL_BOOKING_ERROR("Internal Booking Error. Please Contact support."),
    INVALID_DANCE_CLASS_REQUEST("Dance Class request is invalid."),
    INTERNAL_ERROR("Internal Error. Please contact support."),
    INACTIVE_USER("User is is not active."),
    INVALID_INVITATION_REQUEST("Invitation request is invalid."),
    INVALID_REGISTRATION_REQUEST("Registration request is invalid."),
    INVALID_TOKEN("Invalid token."),
    USER_ERROR("Invalid user request."),
    AUTHENTICATION_ERROR("Authentication Error. Please contact support."),
    INVALID_UPDATE_REQUEST("Update request is invalid.");

    private final String description;
}

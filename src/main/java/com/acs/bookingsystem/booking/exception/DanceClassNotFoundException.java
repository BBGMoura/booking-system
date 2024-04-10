package com.acs.bookingsystem.booking.exception;

import com.acs.bookingsystem.common.exception.ErrorCode;

public class DanceClassNotFoundException extends RuntimeException {
    private final ErrorCode error;

    public DanceClassNotFoundException(String message, ErrorCode error) {
        super(message);
        this.error = error;
    }

    public DanceClassNotFoundException(String message, Throwable cause, ErrorCode error) {
        super(message, cause);
        this.error = error;
    }
}

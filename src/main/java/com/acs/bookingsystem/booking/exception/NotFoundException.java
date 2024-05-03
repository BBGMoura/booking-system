package com.acs.bookingsystem.booking.exception;

import com.acs.bookingsystem.common.exception.ErrorCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotFoundException extends RuntimeException {
    private final ErrorCode error;

    public NotFoundException(String message, ErrorCode error) {
        super(message);
        this.error = error;
    }

    public NotFoundException(String message, Throwable cause, ErrorCode error) {
        super(message, cause);
        this.error = error;
    }
}

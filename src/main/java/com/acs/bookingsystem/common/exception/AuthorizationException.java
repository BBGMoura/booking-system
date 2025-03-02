package com.acs.bookingsystem.common.exception;

import com.acs.bookingsystem.common.exception.model.ErrorCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthorizationException extends RuntimeException {
    private final ErrorCode error;

    public AuthorizationException(String message, ErrorCode error) {
        super(message);
        this.error = error;
    }

    public AuthorizationException(String message, Throwable cause, ErrorCode error) {
        super(message, cause);
        this.error = error;
    }
}
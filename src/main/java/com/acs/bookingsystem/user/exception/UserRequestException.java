package com.acs.bookingsystem.user.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequestException extends RuntimeException {
    private final UserError error;

    public UserRequestException(String message, UserError error) {
        super(message);
        this.error = error;
    }

    public UserRequestException(String message, Throwable cause, UserError error) {
        super(message, cause);
        this.error = error;
    }
}


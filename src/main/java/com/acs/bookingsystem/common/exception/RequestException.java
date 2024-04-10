package com.acs.bookingsystem.common.exception;

import com.acs.bookingsystem.common.exception.ErrorCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestException extends RuntimeException {
    private final ErrorCode error;

    public RequestException(String message, ErrorCode error) {
        super(message);
        this.error = error;
    }

    public RequestException(String message, Throwable cause, ErrorCode error) {
        super(message, cause);
        this.error = error;
    }
}


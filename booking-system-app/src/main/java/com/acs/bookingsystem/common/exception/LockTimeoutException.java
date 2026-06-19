package com.acs.bookingsystem.common.exception;

import com.acs.bookingsystem.common.exception.model.ErrorCode;
import lombok.Getter;

@Getter
public class LockTimeoutException extends RuntimeException {
    private final ErrorCode error;

    public LockTimeoutException(String message, Throwable cause, ErrorCode error) {
        super(message, cause);
        this.error = error;
    }
}

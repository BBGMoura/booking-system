package com.acs.bookingsystem.user.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserRequestException extends RuntimeException {
    private final List<ErrorModel> errors;

    public UserRequestException(List<ErrorModel> errors){
        this.errors = errors;
    }
}

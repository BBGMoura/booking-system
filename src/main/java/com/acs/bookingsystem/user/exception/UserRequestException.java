package com.acs.bookingsystem.user.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UserRequestException extends RuntimeException {
    private List<ErrorModel> errors;

    public UserRequestException(List<ErrorModel> errors){
        this.errors = errors;
    }
}

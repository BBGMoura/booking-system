package com.acs.bookingsystem.common.exception.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class ErrorModel {
    private Date timestamp;
    private int status;
    private String error;
    private String message;
}

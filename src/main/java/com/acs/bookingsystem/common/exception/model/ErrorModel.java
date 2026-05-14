package com.acs.bookingsystem.common.exception.model;

import java.util.Date;
import java.util.List;

public record ErrorModel(
        Date timestamp,
        int status,
        String error,
        String message,
        List<ErrorDetail> details) {
}

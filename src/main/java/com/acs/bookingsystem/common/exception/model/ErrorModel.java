package com.acs.bookingsystem.common.exception.model;

import lombok.Builder;

import java.util.Date;

@Builder
public record ErrorModel (
     Date timestamp,
     int status,
     String error,
     String message) {
}

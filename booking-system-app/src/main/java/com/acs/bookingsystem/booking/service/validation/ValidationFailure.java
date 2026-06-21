package com.acs.bookingsystem.booking.service.validation;

import com.acs.bookingsystem.common.exception.model.ErrorCode;

public record ValidationFailure(String message, ErrorCode code) {}

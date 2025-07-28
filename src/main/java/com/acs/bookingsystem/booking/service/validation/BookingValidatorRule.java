package com.acs.bookingsystem.booking.service.validation;

import com.acs.bookingsystem.booking.request.BookingRequest;

import java.util.Optional;

public interface BookingValidatorRule {
    Optional<String> validate(BookingRequest bookingRequest);
}

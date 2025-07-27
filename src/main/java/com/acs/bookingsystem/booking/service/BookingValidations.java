package com.acs.bookingsystem.booking.service;

import com.acs.bookingsystem.booking.request.BookingRequest;

public interface BookingValidations {
    public void validate(BookingRequest bookingRequest);
}

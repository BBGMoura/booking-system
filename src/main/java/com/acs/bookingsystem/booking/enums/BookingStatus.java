package com.acs.bookingsystem.booking.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BookingStatus {
    BOOKED("B"),
    CANCELLED("C");

    private final String code;
}


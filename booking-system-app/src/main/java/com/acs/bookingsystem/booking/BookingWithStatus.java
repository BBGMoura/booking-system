package com.acs.bookingsystem.booking;

import com.acs.bookingsystem.booking.entity.Booking;
import com.acs.bookingsystem.booking.enums.BookingStatusType;

public record BookingWithStatus(Booking booking, BookingStatusType status) {}

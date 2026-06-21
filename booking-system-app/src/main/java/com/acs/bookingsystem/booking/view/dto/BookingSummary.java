package com.acs.bookingsystem.booking.view.dto;

import com.acs.bookingsystem.booking.enums.BookingStatusType;
import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.danceclass.enums.ClassType;
import java.time.LocalDateTime;
import java.util.UUID;

public record BookingSummary(
    UUID uid,
    Room room,
    ClassType classType,
    BookingStatusType status,
    boolean shareable,
    LocalDateTime bookedFrom,
    LocalDateTime bookedTo)
    implements BookingView {}

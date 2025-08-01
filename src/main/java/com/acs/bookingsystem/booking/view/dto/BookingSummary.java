package com.acs.bookingsystem.booking.view.dto;

import com.acs.bookingsystem.danceclass.entity.DanceClass;
import com.acs.bookingsystem.booking.enums.Room;

import java.time.LocalDateTime;

public record BookingSummary(int id,
                             Room room,
                             DanceClass danceClass,
                             boolean active,
                             boolean shareable,
                             LocalDateTime bookedFrom,
                             LocalDateTime bookedTo) implements BookingView { }
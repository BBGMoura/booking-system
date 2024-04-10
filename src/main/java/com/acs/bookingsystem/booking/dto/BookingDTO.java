package com.acs.bookingsystem.booking.dto;

import com.acs.bookingsystem.booking.entities.DanceClass;
import com.acs.bookingsystem.booking.enums.Room;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingDTO(int id,
                         int userId,
                         Room room,
                         int danceClassId,
                         LocalDateTime dateFrom,
                         LocalDateTime dateTo,
                         BigDecimal totalPrice
) {
}

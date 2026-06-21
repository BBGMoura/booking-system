package com.acs.bookingsystem.booking.view.dto;

import com.acs.bookingsystem.booking.enums.BookingStatusType;
import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.danceclass.enums.ClassType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BookingDetail(
    UUID uid,
    UUID userUid,
    Room room,
    BookingStatusType status,
    boolean shareable,
    ClassType classType,
    LocalDateTime dateFrom,
    LocalDateTime dateTo,
    BigDecimal totalPrice)
    implements BookingView {}

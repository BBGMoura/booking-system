package com.acs.bookingsystem.booking.view.dto;

import com.acs.bookingsystem.booking.enums.BookingStatusType;
import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.danceclass.enums.ClassType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record BookingDetail(
    UUID uid,
    UUID userUid,
    Room room,
    BookingStatusType status,
    boolean shareable,
    ClassType classType,
    OffsetDateTime dateFrom,
    OffsetDateTime dateTo,
    BigDecimal totalPrice)
    implements BookingView {}

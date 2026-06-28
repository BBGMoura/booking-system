package com.acs.bookingsystem.booking.request;

import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.danceclass.enums.ClassType;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record BookingRequest(
    @NotNull Room room,
    @NotNull ClassType classType,
    boolean isShareable,
    @NotNull OffsetDateTime dateFrom,
    @NotNull OffsetDateTime dateTo) {}

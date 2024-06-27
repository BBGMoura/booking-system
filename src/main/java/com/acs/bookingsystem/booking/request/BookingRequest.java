package com.acs.bookingsystem.booking.request;

import com.acs.bookingsystem.booking.enums.ClassType;
import com.acs.bookingsystem.booking.enums.Room;
import jakarta.validation.constraints.NotNull;



import java.time.LocalDateTime;

public record BookingRequest(@NotNull int userId,
                             @NotNull Room room,
                             @NotNull ClassType classType,
                             boolean isShareable,
                             @NotNull LocalDateTime dateFrom,
                             @NotNull LocalDateTime dateTo) {
}

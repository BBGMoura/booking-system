package com.acs.bookingsystem.booking.dto;

import com.acs.bookingsystem.booking.enums.ClassType;
import com.acs.bookingsystem.booking.enums.Room;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequest {
    @NotNull
    private int userId;
    @NotNull
    private Room room;
    @NotNull
    private ClassType classType;
    @NotNull
    private LocalDateTime dateFrom;
    @NotNull
    private LocalDateTime dateTo;
}

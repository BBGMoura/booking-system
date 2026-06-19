package com.acs.bookingsystem.danceclass.request;

import com.acs.bookingsystem.danceclass.enums.ClassType;
import com.acs.bookingsystem.user.enums.Role;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;


public record DanceClassRequest(@NotNull(message = "Class type cannot be null")
                                ClassType classType,
                                @DecimalMin(value = "0.0")
                                @DecimalMax(value = "1000")
                                @Digits(integer = 3, fraction = 2) @NotNull
                                BigDecimal pricePerHour,
                                @NotNull(message = "Role cannot be null")
                                Role role) {
}

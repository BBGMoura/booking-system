package com.acs.bookingsystem.booking.request;

import com.acs.bookingsystem.booking.enums.ClassType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DanceClassRequest {
    @NotNull
    ClassType classType;
    @DecimalMin(value = "0.0")
    @Digits(integer = 3, fraction = 2)
    @NotNull
    BigDecimal pricePerHour;
}

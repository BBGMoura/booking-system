package com.acs.bookingsystem.booking.request;

import com.acs.bookingsystem.booking.enums.ClassType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DanceClassRequest {
    @NotBlank
    ClassType classType;
    BigDecimal pricePer60;
    BigDecimal pricePer45;
    BigDecimal pricePer30;
}

package com.acs.bookingsystem.payment;

import com.acs.bookingsystem.booking.entities.DanceClass;
import com.acs.bookingsystem.booking.enums.ClassType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PriceCalculatorTest {
    @Test
    void calculateValidTimeSlot(){
        DanceClass danceClass = getDanceClass();
        LocalDateTime start = getStart();

        BigDecimal totalPrice = PriceCalculator.calculateTotalPrice(
                start,
                LocalDateTime.of(2024, Month.NOVEMBER,12,12,30,0),
                danceClass);
        assertEquals(BigDecimal.TEN, totalPrice);

        totalPrice = PriceCalculator.calculateTotalPrice(start,
                LocalDateTime.of(2024, Month.NOVEMBER,12,12,45,0),
                danceClass);
        assertEquals(BigDecimal.TWO, totalPrice);

        totalPrice = PriceCalculator.calculateTotalPrice(start,
                LocalDateTime.of(2024, Month.NOVEMBER,12,13,0,0),
                danceClass);
        assertEquals(BigDecimal.ONE, totalPrice);

        totalPrice = PriceCalculator.calculateTotalPrice(start,
                LocalDateTime.of(2024, Month.NOVEMBER,12,14,30,0),
                danceClass);
        assertEquals(BigDecimal.valueOf(12), totalPrice);
    }

    private LocalDateTime getStart(){
        return LocalDateTime.of(2024, Month.NOVEMBER,12,12,0,0);
    }

    private DanceClass getDanceClass(){
        return new DanceClass(1, ClassType.PRIVATE,true, BigDecimal.ONE,BigDecimal.TWO,BigDecimal.TEN);
    }
}
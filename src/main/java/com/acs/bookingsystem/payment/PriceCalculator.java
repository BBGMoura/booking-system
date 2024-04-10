package com.acs.bookingsystem.payment;

import com.acs.bookingsystem.booking.entities.DanceClass;
import com.acs.bookingsystem.booking.exception.DanceClassNotFoundException;
import com.acs.bookingsystem.common.exception.ErrorCode;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

public class PriceCalculator {
    static final long INTERVAL_60 = 60;
    static final long INTERVAL_45 = 45;
    static final long INTERVAL_30 = 30;

    private PriceCalculator() {
        throw new IllegalStateException("Price Calculator is a util class.");
    }

    public static BigDecimal calculateTotalPrice(LocalDateTime dateFrom, LocalDateTime dateTo,  DanceClass danceClass){
        if (!danceClass.isActive()) {
            throw new DanceClassNotFoundException(String.format("Dance class type %s is not active",danceClass.getClassType()), ErrorCode.INVALID_BOOKING_REQUEST);
        }

        if (danceClass.getPricePer30() == null || danceClass.getPricePer45() == null || danceClass.getPricePer60() == null) {
            return BigDecimal.ZERO;
        }

        Duration duration = Duration.between(dateFrom, dateTo);
        long durationInMins = duration.toMinutes();

        int pay60 = 0;
        int pay45 = 0;
        int pay30 = 0;

        while (durationInMins > 0) {
            if (durationInMins >= INTERVAL_60) {
                pay60++;
                durationInMins -= INTERVAL_60;
            } else if (durationInMins >= INTERVAL_45) {
                pay45++;
                durationInMins -= INTERVAL_45;
            } else if (durationInMins >= INTERVAL_30) {
                pay30++;
                durationInMins -= INTERVAL_30;
            }
        }

        BigDecimal costFor60Minutes = danceClass.getPricePer60().multiply(BigDecimal.valueOf(pay60));
        BigDecimal costFor45Minutes = danceClass.getPricePer45().multiply(BigDecimal.valueOf(pay45));
        BigDecimal costFor30Minutes = danceClass.getPricePer30().multiply(BigDecimal.valueOf(pay30));

        BigDecimal totalCost = costFor60Minutes.add(costFor45Minutes).add(costFor30Minutes);

        System.out.println("Total cost: £" + totalCost);

        return totalCost;
    }
}

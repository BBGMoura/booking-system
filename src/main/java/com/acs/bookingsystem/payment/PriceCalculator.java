package com.acs.bookingsystem.payment;

import com.acs.bookingsystem.booking.entity.DanceClass;
import com.acs.bookingsystem.common.exception.NotFoundException;
import com.acs.bookingsystem.common.exception.model.ErrorCode;
import com.acs.bookingsystem.common.exception.RequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

public class PriceCalculator {

    public static final Logger LOG = LoggerFactory.getLogger(PriceCalculator.class);

    public static BigDecimal calculateTotalPrice(LocalDateTime dateFrom, LocalDateTime dateTo, DanceClass danceClass) {
        BigDecimal pricePerHour = danceClass.getPricePerHour();
        if (pricePerHour == null) {
            return BigDecimal.ZERO;
        }

        long minutes = Duration.between(dateFrom, dateTo).toMinutes();
        if (minutes <= 0) {
            throw new RequestException("Cannot complete booking as time interval is 0.", ErrorCode.INVALID_BOOKING_REQUEST);
        }

        return pricePerHour.multiply(BigDecimal.valueOf(minutes)).divide(BigDecimal.valueOf(60), RoundingMode.HALF_UP);
    }
}

package com.acs.bookingsystem.payment;

import com.acs.bookingsystem.danceclass.entity.DanceClass;
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

    private static final int CURRENCY_SCALE = 2;
    private static final int MINUTES_PER_HOUR = 60;

    private PriceCalculator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Calculates the total price for a booking based on duration and class price.
     *
     * @param dateFrom   The booking start time.
     * @param dateTo     The booking end time.
     * @param danceClass The dance class containing the price per hour.
     * @return The calculated total price.
     * @throws RequestException if the booking duration is invalid.
     */
    public static BigDecimal calculateTotalPrice(LocalDateTime dateFrom, LocalDateTime dateTo, DanceClass danceClass) {
        BigDecimal pricePerHour = danceClass.getPricePerHour();
        if (pricePerHour == null) {
            return BigDecimal.ZERO;
        }

        long minutes = Duration.between(dateFrom, dateTo).toMinutes();

        if (minutes <= 0) {
            throw new RequestException("Cannot complete booking as time interval is 0.",
                                       ErrorCode.INVALID_BOOKING_REQUEST);
        }

        BigDecimal totalPrice = pricePerHour.multiply(BigDecimal.valueOf(minutes))
                                            .divide(BigDecimal.valueOf(MINUTES_PER_HOUR),
                                                    CURRENCY_SCALE,
                                                    RoundingMode.HALF_UP);

        LOG.info("""
                         Price calculation complete for booking:
                             - Class: {} (ID: {})
                             - Time: {} to {}
                             - Duration: {} minutes
                             - Price per hour: {}
                             - Total price: {}""",
                 danceClass.getClassType(),
                 danceClass.getId(),
                 dateFrom,
                 dateTo,
                 minutes,
                 pricePerHour,
                 totalPrice);

        return totalPrice;
    }
}

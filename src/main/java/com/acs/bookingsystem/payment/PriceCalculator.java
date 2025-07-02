package com.acs.bookingsystem.payment;

import com.acs.bookingsystem.booking.entity.DanceClass;
import com.acs.bookingsystem.common.exception.NotFoundException;
import com.acs.bookingsystem.common.exception.model.ErrorCode;
import com.acs.bookingsystem.common.exception.RequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

public class PriceCalculator {
    public static final Logger LOG = LoggerFactory.getLogger(PriceCalculator.class);

    private PriceCalculator() {
        throw new IllegalStateException("Price Calculator is an util class.");
    }


    public static BigDecimal calculateTotalPrice(LocalDateTime dateFrom, LocalDateTime dateTo,  DanceClass danceClass){
        if (!danceClass.isActive()) {
            throw new NotFoundException("Dance class type"+danceClass.getClassType()+ "is not active", ErrorCode.INVALID_BOOKING_REQUEST);
        }

        if (danceClass.getPricePerHour() == null) {
            return BigDecimal.ZERO;
        }

        long durationInMins = Duration.between(dateFrom, dateTo).toMinutes();

        if (durationInMins == 0) {
            throw new RequestException("Cannot complete booking as time interval is 0 minutes.", ErrorCode.INVALID_BOOKING_REQUEST);
        }

        return BigDecimal.ZERO;

//        int pay60 = 0;
//        int pay45 = 0;
//        int pay30 = 0;
//
//        while (durationInMins > 0) {
//           if (durationInMins % INTERVAL_60 == 0 ||
//                    (durationInMins / INTERVAL_60 > 0 && (durationInMins % INTERVAL_60 == INTERVAL_45 || durationInMins % INTERVAL_60 == INTERVAL_30))) {
//                pay60++;
//                durationInMins -= INTERVAL_60;
//            } else if (durationInMins % INTERVAL_45  == 0 ||
//                    (durationInMins / INTERVAL_45  > 0 && durationInMins % INTERVAL_45 == INTERVAL_30)) {
//                pay45++;
//                durationInMins -= INTERVAL_45;
//            } else if (durationInMins % INTERVAL_30 == 0) {
//                pay30++;
//                durationInMins -= INTERVAL_30;
//            } else {
//                throw new RequestException("Cannot complete booking as time interval is invalid.", ErrorCode.INVALID_BOOKING_REQUEST);
//            }
//        }
//
//        BigDecimal costFor60Minutes = danceClass.getPricePer60().multiply(BigDecimal.valueOf(pay60));
//        BigDecimal costFor45Minutes = danceClass.getPricePer45().multiply(BigDecimal.valueOf(pay45));
//        BigDecimal costFor30Minutes = danceClass.getPricePer30().multiply(BigDecimal.valueOf(pay30));
//        BigDecimal totalCost = costFor60Minutes.add(costFor45Minutes).add(costFor30Minutes);
//
//        //TODO: improve this implementation to return an object with these values? as the logging should have the booking request id.
//        LOG.debug("60 mins dance class: {} classes for price: {}. Total : {}", pay60, danceClass.getPricePer60(), costFor60Minutes);
//        LOG.debug("45 mins dance class: {} classes for price: {}. Total : {}", pay45, danceClass.getPricePer45(), costFor45Minutes);
//        LOG.debug("30 mins dance class: {} classes for price: {}. Total : {}", pay30, danceClass.getPricePer60(), costFor30Minutes);
//        LOG.debug("Total Cost : {}", totalCost);
//
//        return totalCost;
    }
}

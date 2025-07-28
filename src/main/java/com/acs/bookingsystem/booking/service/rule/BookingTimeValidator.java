package com.acs.bookingsystem.booking.service.rule;

import com.acs.bookingsystem.booking.config.ScheduleConfig;
import com.acs.bookingsystem.booking.request.BookingRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Component
@AllArgsConstructor
public class BookingTimeValidator implements BookingValidatorRule {

    private final ScheduleConfig scheduleConfig;

    private static final Duration TIME_INTERVAL = Duration.ofMinutes(5);
    private static final Duration MINIMUM_BOOKING_TIME = Duration.ofMinutes(15);

    public Optional<String> validate(BookingRequest bookingRequest) {
        if (endsBeforeStarts(bookingRequest.dateFrom(), bookingRequest.dateTo())) {
            return Optional.of("Booking start time is after end time.");
        }

        if (isTooShort(bookingRequest)) {
            return Optional.of("Booking must be a minimum of 15 minutes.");
        }

        if (hasInvalidTimeInterval(bookingRequest)) {
            return Optional.of("Booking interval is invalid. Must be in intervals of 5 minutes.");
        }

        if (isNotSameDate(bookingRequest.dateFrom(), bookingRequest.dateTo())) {
            return Optional.of("Booking must start and end on the same day.");
        }

        if (isNotWithinOpeningHours(bookingRequest.dateFrom(), bookingRequest.dateTo())) {
            return Optional.of("Cannot make a booking as booking time is not within opening times.");
        }

        return Optional.empty();
    }

    private boolean endsBeforeStarts(LocalDateTime dateFrom, LocalDateTime dateTo) {
        return dateFrom.isAfter(dateTo);
    }

    private boolean isTooShort(BookingRequest bookingRequest) {
        Duration duration = Duration.between(bookingRequest.dateFrom(), bookingRequest.dateTo());
        return duration.compareTo(MINIMUM_BOOKING_TIME) < 0;
    }

    private boolean hasInvalidTimeInterval(BookingRequest bookingRequest) {
        Duration duration = Duration.between(bookingRequest.dateFrom(), bookingRequest.dateTo());
        return duration.toMinutes() % TIME_INTERVAL.toMinutes() != 0;
    }

    private boolean isNotSameDate(LocalDateTime dateFrom, LocalDateTime dateTo) {
        return !dateFrom.toLocalDate().equals(dateTo.toLocalDate());
    }

    private boolean isNotWithinOpeningHours(LocalDateTime dateFrom, LocalDateTime dateTo) {
        final DayOfWeek dayOfWeek = dateFrom.getDayOfWeek();
        LocalTime openingTime;
        LocalTime closingTime;

        switch (dayOfWeek) {
            case DayOfWeek.SATURDAY -> {
                openingTime = scheduleConfig.getSaturdayOpening();
                closingTime = scheduleConfig.getSaturdayClosing();
            }
            case DayOfWeek.SUNDAY -> {
                openingTime = scheduleConfig.getSundayOpening();
                closingTime = scheduleConfig.getSundayClosing();
            }
            default -> {
                openingTime = scheduleConfig.getWeekdayOpening();
                closingTime = scheduleConfig.getWeekdayClosing();
            }
        }

        return validateTimeRange(dateFrom,openingTime,closingTime) || validateTimeRange(dateTo,openingTime,closingTime);
    }

    private boolean validateTimeRange(LocalDateTime dateTime, LocalTime openingTime, LocalTime closingTime) {
        return dateTime.toLocalTime().isBefore(openingTime) || dateTime.toLocalTime().isAfter(closingTime);
    }
}

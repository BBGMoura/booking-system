package com.acs.bookingsystem.booking.service.validation;

import com.acs.bookingsystem.booking.request.BookingRequest;
import com.acs.bookingsystem.common.exception.model.ErrorCode;
import lombok.RequiredArgsConstructor;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@RequiredArgsConstructor
public class BookingTimeValidator implements BookingValidatorRule {

    private final ScheduleProperties scheduleProperties;

    private static final Duration TIME_INTERVAL = Duration.ofMinutes(5);
    private static final Duration MINIMUM_BOOKING_TIME = Duration.ofMinutes(15);

    public Optional<ValidationFailure> validate(BookingRequest bookingRequest) {
        if (endsBeforeStarts(bookingRequest.dateFrom(), bookingRequest.dateTo())) {
            return failure("Booking start time must be before end time.");
        }

        if (isTooShort(bookingRequest)) {
            return failure("Booking must be a minimum of 15 minutes.");
        }

        if (isInvalidMinuteAlignment(bookingRequest.dateFrom())) {
            return failure("Booking start time must be on a 5-minute interval (e.g., :00, :05, :10), with no seconds or milliseconds.");
        }

        if (isInvalidMinuteAlignment(bookingRequest.dateTo())) {
            return failure("Booking end time must be on a 5-minute interval (e.g., :00, :05, :10), with no seconds or milliseconds.");
        }

        if (hasInvalidTimeInterval(bookingRequest)) {
            return failure("Booking duration must be in intervals of 5 minutes.");
        }

        if (isNotSameDate(bookingRequest.dateFrom(), bookingRequest.dateTo())) {
            return failure("Booking must start and end on the same day.");
        }

        if (isNotWithinOpeningHours(bookingRequest.dateFrom(), bookingRequest.dateTo())) {
            return failure("Booking time is not within opening hours.");
        }

        return Optional.empty();
    }

    private static Optional<ValidationFailure> failure(String message) {
        return Optional.of(new ValidationFailure(message, ErrorCode.BOOKING_TIME_INVALID));
    }

    private boolean endsBeforeStarts(LocalDateTime dateFrom, LocalDateTime dateTo) {
        return !dateFrom.isBefore(dateTo);
    }

    private boolean isTooShort(BookingRequest bookingRequest) {
        Duration duration = Duration.between(bookingRequest.dateFrom(), bookingRequest.dateTo());
        return duration.compareTo(MINIMUM_BOOKING_TIME) < 0;
    }

    private boolean isInvalidMinuteAlignment(LocalDateTime dateTime) {
        return (dateTime.getSecond() != 0 ||
                dateTime.getNano() != 0 ||
                dateTime.getMinute() % TIME_INTERVAL.toMinutes() != 0);
    }

    private boolean hasInvalidTimeInterval(BookingRequest bookingRequest) {
        Duration duration = Duration.between(bookingRequest.dateFrom(), bookingRequest.dateTo());
        return duration.toMillis() % TIME_INTERVAL.toMillis() != 0;
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
                openingTime = scheduleProperties.getSaturday().getOpening();
                closingTime = scheduleProperties.getSaturday().getClosing();
            }
            case DayOfWeek.SUNDAY -> {
                openingTime = scheduleProperties.getSunday().getOpening();
                closingTime = scheduleProperties.getSunday().getClosing();
            }
            default -> {
                openingTime = scheduleProperties.getWeekday().getOpening();
                closingTime = scheduleProperties.getWeekday().getClosing();
            }
        }

        return validateTimeRange(dateFrom, openingTime, closingTime) || validateTimeRange(dateTo, openingTime, closingTime);
    }

    private boolean validateTimeRange(LocalDateTime date, LocalTime openingTime, LocalTime closingTime) {
        return date.toLocalTime().isBefore(openingTime) || date.toLocalTime().isAfter(closingTime);
    }
}

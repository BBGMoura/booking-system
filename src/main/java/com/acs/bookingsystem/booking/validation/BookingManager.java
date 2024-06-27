package com.acs.bookingsystem.booking.validation;

import com.acs.bookingsystem.booking.config.ScheduleConfig;
import com.acs.bookingsystem.booking.entities.Booking;
import com.acs.bookingsystem.booking.repository.BookingRepository;
import com.acs.bookingsystem.booking.request.BookingRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class BookingManager {
    private final BookingRepository bookingRepository;
    private final ScheduleConfig scheduleConfig;
    private static final int MINIMUM_BOOKING_TIME = 15;
    private static final int TIME_INTERVAL = 5;
    private static final int MAXIMUM_SLOTS = 3;

    public Optional<String> validateBookingTime(BookingRequest bookingRequest) {
        if(isNotLongerThan15Minutes(bookingRequest)) {
            return Optional.of("Booking must be a minimum of 15 minutes.");
        }

        if (isNotIntervalOf5Minutes(bookingRequest)) {
            return Optional.of("Booking interval is invalid. Must be in intervals of 5 minutes.");
        }

        if (endsBeforeStarts(bookingRequest.dateFrom(), bookingRequest.dateTo())) {
            return Optional.of("Booking start time is after end time.");
        }

        if (overlapsNonShareableExistingBookings(bookingRequest)) {
            return Optional.of("Booking timeslot is unavailable.");
        }

        if (isNotSameDate(bookingRequest.dateFrom(), bookingRequest.dateTo())) {
            return Optional.of("Booking must start and end on the same day.");
        }

        if (isNotWithinOpeningTime(bookingRequest.dateFrom(), bookingRequest.dateTo())) {
            return Optional.of("Cannot make a booking as booking time is not within opening times.");
        }

        List<Booking> shareableBookings = getBookings(bookingRequest, true);

        if (isNotShareableAndOverlapsShareableBookings(bookingRequest, shareableBookings)) {
            return Optional.of("Cannot make a non-shareable booking which overlaps other bookings.");
        }

        if (overlapsThreeOrMoreShareableBookings(shareableBookings, bookingRequest)) {
            return Optional.of("Cannot make booking as timeslot is unavailable.");
        }

        return Optional.empty();
    }

    private boolean isNotIntervalOf5Minutes(BookingRequest bookingRequest) {
        long duration = Duration.between(bookingRequest.dateFrom(), bookingRequest.dateTo()).toMinutes();
        return duration % TIME_INTERVAL != 0;
    }

    private boolean isNotLongerThan15Minutes(BookingRequest bookingRequest) {
        long duration = Duration.between(bookingRequest.dateFrom(), bookingRequest.dateTo()).toMinutes();
        return duration < MINIMUM_BOOKING_TIME;
    }

    private boolean endsBeforeStarts(LocalDateTime dateFrom, LocalDateTime dateTo) {
        return dateFrom.isAfter(dateTo);
    }

    private boolean overlapsNonShareableExistingBookings(BookingRequest bookingRequest) {
        List<Booking> bookings = getBookings(bookingRequest, false);
        return !bookings.isEmpty();
    }

    private boolean isNotSameDate(LocalDateTime dateFrom, LocalDateTime dateTo) {
        return !dateFrom.toLocalDate().equals(dateTo.toLocalDate());
    }

    private boolean isNotWithinOpeningTime(LocalDateTime dateFrom, LocalDateTime dateTo) {
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

    private static boolean isNotShareableAndOverlapsShareableBookings(BookingRequest bookingRequest, List<Booking> shareableBookings) {
        return !bookingRequest.isShareable() && !shareableBookings.isEmpty();
    }

    private boolean overlapsThreeOrMoreShareableBookings(List<Booking> shareableBookings, BookingRequest bookingRequest) {
       LocalDateTime start = bookingRequest.dateFrom();
       final LocalDateTime end = bookingRequest.dateTo();

        while(start.isBefore(end)) {
            int overlapCounter = 0;

            for (Booking booking : shareableBookings) {
                if (bookingsOverlap(start, start.plusMinutes(5), booking)) {
                    overlapCounter++;
                }
            }

            if (overlapCounter >= MAXIMUM_SLOTS) {
                return true;
            }

            start = start.plusMinutes(TIME_INTERVAL);
        }
        return false;
    }

    private boolean bookingsOverlap(LocalDateTime start, LocalDateTime end, Booking booking) {
        return booking.getBookedFrom().isBefore(end) && booking.getBookedTo().isAfter(start);
    }

    private List<Booking> getBookings(BookingRequest bookingRequest, boolean shareable) {
        return bookingRepository.findActiveBookingsByRoomAndEndOrStartBetweenTimeRange(bookingRequest.room(),
                                                                                       shareable,
                                                                                       bookingRequest.dateFrom(),
                                                                                       bookingRequest.dateTo());
    }
}

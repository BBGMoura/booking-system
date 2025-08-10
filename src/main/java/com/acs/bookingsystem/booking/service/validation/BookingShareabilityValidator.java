package com.acs.bookingsystem.booking.service.validation;

import com.acs.bookingsystem.booking.entity.Booking;
import com.acs.bookingsystem.booking.repository.BookingRepository;
import com.acs.bookingsystem.booking.request.BookingRequest;
import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Order(3)
@Component
@AllArgsConstructor
public class BookingShareabilityValidator implements BookingValidatorRule {

    private final BookingRepository bookingRepository;

    private static final int MAXIMUM_SLOTS = 3;
    private static final int TIME_INTERVAL = 5;

    public Optional<String> validate(BookingRequest bookingRequest) {
        List<Booking> shareableBookings = getBookings(bookingRequest);

        if (isNotShareableAndOverlapsShareableBookings(bookingRequest, shareableBookings)) {
            return Optional.of("Cannot make a non-shareable booking which overlaps other bookings.");
        }

        if (overlapsMaximumShareableBookings(shareableBookings, bookingRequest)) {
            return Optional.of("Timeslot timeslot is unavailable. Can only book " + MAXIMUM_SLOTS + " shareable bookings at a time.");
        }

        return Optional.empty();
    }

    private static boolean isNotShareableAndOverlapsShareableBookings(BookingRequest bookingRequest, List<Booking> shareableBookings) {
        return !bookingRequest.isShareable() && !shareableBookings.isEmpty();
    }

    private boolean overlapsMaximumShareableBookings(List<Booking> shareableBookings, BookingRequest bookingRequest) {
        LocalDateTime start = bookingRequest.dateFrom();
        final LocalDateTime end = bookingRequest.dateTo();

        while(start.isBefore(end)) {
            int overlapCounter = 0;

            LocalDateTime slotEnd = start.plusMinutes(TIME_INTERVAL);

            for (Booking booking : shareableBookings) {
                if (bookingsOverlap(start, slotEnd, booking)) {
                    overlapCounter++;
                }
            }

            if (overlapCounter >= MAXIMUM_SLOTS) {
                return true;
            }

            start = slotEnd;
        }
        return false;
    }

    private boolean bookingsOverlap(LocalDateTime start, LocalDateTime end, Booking booking) {
        return booking.getBookedFrom().isBefore(end) && booking.getBookedTo().isAfter(start);
    }

    private List<Booking> getBookings(BookingRequest bookingRequest) {
        return bookingRepository.findActiveBookingsForRoomAndTimeRange(bookingRequest.room(),
                                                                       Boolean.TRUE,
                                                                       bookingRequest.dateFrom(),
                                                                       bookingRequest.dateTo());
    }
}

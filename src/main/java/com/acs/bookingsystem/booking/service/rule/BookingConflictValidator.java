package com.acs.bookingsystem.booking.service.rule;

import com.acs.bookingsystem.booking.entity.Booking;
import com.acs.bookingsystem.booking.repository.BookingRepository;
import com.acs.bookingsystem.booking.request.BookingRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class BookingConflictValidator implements BookingValidatorRule {

    private final BookingRepository bookingRepository;

    public Optional<String> validate(BookingRequest bookingRequest) {
        if (hasConflictingBookings(bookingRequest)) {
            return Optional.of("Booking timeslot is unavailable.");
        }
        return Optional.empty();
    }

    private boolean hasConflictingBookings(BookingRequest bookingRequest) {
        List<Booking> bookings = getBookings(bookingRequest);
        return !bookings.isEmpty();
    }

    private List<Booking> getBookings(BookingRequest bookingRequest) {
        return bookingRepository.findActiveBookingsByRoomAndEndOrStartBetweenTimeRange(bookingRequest.room(),
                                                                                       Boolean.FALSE,
                                                                                       bookingRequest.dateFrom(),
                                                                                       bookingRequest.dateTo());
    }
}

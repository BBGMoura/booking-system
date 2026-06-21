package com.acs.bookingsystem.booking.service.validation;

import com.acs.bookingsystem.booking.repository.BookingRepository;
import com.acs.bookingsystem.booking.request.BookingRequest;
import com.acs.bookingsystem.common.exception.model.ErrorCode;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NonShareableConflictValidator implements BookingValidatorRule {

  private final BookingRepository bookingRepository;

  public Optional<ValidationFailure> validate(BookingRequest bookingRequest) {
    if (hasConflictingBookings(bookingRequest)) {
      return Optional.of(
          new ValidationFailure("Booking timeslot is unavailable.", ErrorCode.BOOKING_CONFLICT));
    }
    return Optional.empty();
  }

  private boolean hasConflictingBookings(BookingRequest bookingRequest) {
    return !bookingRepository
        .findActiveBookingsForRoomAndTimeRange(
            bookingRequest.room(), false, bookingRequest.dateFrom(), bookingRequest.dateTo())
        .isEmpty();
  }
}

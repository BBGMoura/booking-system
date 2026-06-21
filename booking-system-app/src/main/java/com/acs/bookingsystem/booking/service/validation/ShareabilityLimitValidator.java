package com.acs.bookingsystem.booking.service.validation;

import com.acs.bookingsystem.booking.entity.Booking;
import com.acs.bookingsystem.booking.repository.BookingRepository;
import com.acs.bookingsystem.booking.request.BookingRequest;
import com.acs.bookingsystem.common.exception.model.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ShareabilityLimitValidator implements BookingValidatorRule {

  private final BookingRepository bookingRepository;

  private static final int MAXIMUM_SLOTS = 3;
  private static final int TIME_INTERVAL = 5;

  public Optional<ValidationFailure> validate(BookingRequest bookingRequest) {
    List<Booking> shareableBookings = getBookings(bookingRequest);

    if (isNotShareableAndOverlapsShareableBookings(bookingRequest, shareableBookings)) {
      return Optional.of(
          new ValidationFailure(
              "Cannot make a non-shareable booking which overlaps other bookings.",
              ErrorCode.BOOKING_CONFLICT));
    }

    if (overlapsMaximumShareableBookings(shareableBookings, bookingRequest)) {
      return Optional.of(
          new ValidationFailure(
              "Timeslot is unavailable. Can only book "
                  + MAXIMUM_SLOTS
                  + " shareable bookings at a time.",
              ErrorCode.BOOKING_SHAREABLE_LIMIT));
    }

    return Optional.empty();
  }

  private static boolean isNotShareableAndOverlapsShareableBookings(
      BookingRequest bookingRequest, List<Booking> shareableBookings) {
    return !bookingRequest.isShareable() && !shareableBookings.isEmpty();
  }

  private boolean overlapsMaximumShareableBookings(
      List<Booking> shareableBookings, BookingRequest bookingRequest) {
    LocalDateTime start = bookingRequest.dateFrom();
    final LocalDateTime end = bookingRequest.dateTo();

    while (start.isBefore(end)) {
      LocalDateTime slotEnd = start.plusMinutes(TIME_INTERVAL);
      int overlapCounter = 0;

      for (Booking booking : shareableBookings) {
        if (bookingsOverlap(start, slotEnd, booking)) {
          overlapCounter++;
        }
      }

      if (overlapCounter >= MAXIMUM_SLOTS) return true;
      start = slotEnd;
    }
    return false;
  }

  private boolean bookingsOverlap(LocalDateTime start, LocalDateTime end, Booking booking) {
    return booking.getBookedFrom().isBefore(end) && booking.getBookedTo().isAfter(start);
  }

  private List<Booking> getBookings(BookingRequest bookingRequest) {
    return bookingRepository.findActiveBookingsForRoomAndTimeRange(
        bookingRequest.room(), true, bookingRequest.dateFrom(), bookingRequest.dateTo());
  }
}

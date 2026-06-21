package com.acs.bookingsystem.booking.service;

import com.acs.bookingsystem.booking.entity.Booking;
import com.acs.bookingsystem.booking.entity.BookingStatus;
import com.acs.bookingsystem.booking.enums.BookingStatusType;
import com.acs.bookingsystem.booking.repository.BookingStatusRepository;
import com.acs.bookingsystem.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingStatusService {

  private final BookingStatusRepository bookingStatusRepository;

  public void save(Booking booking, User createdBy, BookingStatusType status) {
    bookingStatusRepository.save(
        BookingStatus.builder().booking(booking).status(status).createdBy(createdBy).build());
  }

  public BookingStatusType resolveStatus(Booking booking) {
    return bookingStatusRepository
        .findLatestStatusByBookingId(booking.getId())
        .orElse(BookingStatusType.BOOKED);
  }
}

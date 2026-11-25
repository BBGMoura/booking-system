package com.acs.bookingsystem.booking.view;

import com.acs.bookingsystem.booking.entity.Booking;
import com.acs.bookingsystem.booking.enums.BookingStatusType;
import com.acs.bookingsystem.booking.view.dto.BookingDetail;
import com.acs.bookingsystem.booking.view.dto.BookingSummary;
import com.acs.bookingsystem.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

  public BookingDetail mapDetail(Booking booking, BookingStatusType status) {
    User bookedFor = booking.getBookedFor();
    return new BookingDetail(
        booking.getUid(),
        bookedFor == null ? null : bookedFor.getUid(),
        bookedFor == null ? null : bookedFor.getFirstName(),
        bookedFor == null ? null : bookedFor.getLastName(),
        booking.getRoom(),
        status,
        booking.isShareable(),
        booking.getBookingType(),
        booking.getBookedFrom(),
        booking.getBookedTo(),
        booking.getTotalPrice(),
        booking.isForStudio());
  }

  public BookingSummary mapSummary(Booking booking, BookingStatusType status) {
    return new BookingSummary(
        booking.getUid(),
        booking.getRoom(),
        booking.getBookingType(),
        status,
        booking.isShareable(),
        booking.getBookedFrom(),
        booking.getBookedTo(),
        booking.isForStudio());
  }
}

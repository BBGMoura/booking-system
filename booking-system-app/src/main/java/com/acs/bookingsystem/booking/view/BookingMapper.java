package com.acs.bookingsystem.booking.view;

import com.acs.bookingsystem.booking.entity.Booking;
import com.acs.bookingsystem.booking.enums.BookingStatusType;
import com.acs.bookingsystem.booking.view.dto.BookingDetail;
import com.acs.bookingsystem.booking.view.dto.BookingSummary;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

  public BookingDetail mapDetail(Booking booking, BookingStatusType status) {
    return new BookingDetail(
        booking.getUid(),
        booking.getUser().getUid(),
        booking.getRoom(),
        status,
        booking.isShareable(),
        booking.getDanceClass().getClassType(),
        booking.getBookedFrom(),
        booking.getBookedTo(),
        booking.getTotalPrice());
  }

  public BookingSummary mapSummary(Booking booking, BookingStatusType status) {
    return new BookingSummary(
        booking.getUid(),
        booking.getRoom(),
        booking.getDanceClass().getClassType(),
        status,
        booking.isShareable(),
        booking.getBookedFrom(),
        booking.getBookedTo());
  }
}

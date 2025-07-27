package com.acs.bookingsystem.booking.view;

import com.acs.bookingsystem.booking.entity.Booking;

import com.acs.bookingsystem.booking.view.dto.BookingDetail;
import com.acs.bookingsystem.booking.view.dto.BookingSummary;
import org.springframework.stereotype.Service;

@Service
public class BookingMapper {
    public BookingDetail mapDetail(Booking booking) {
        return new BookingDetail(booking.getId(),
                                 booking.getUser().getId(),
                                 booking.getRoom(),
                                 booking.isActive(),
                                 booking.isShareable(),
                                 booking.getDanceClass().getId(),
                                 booking.getBookedFrom(),
                                 booking.getBookedTo(),
                                 booking.getTotalPrice());
    }

    public BookingSummary mapSummary(Booking booking) {
        return new BookingSummary(booking.getId(),
                                  booking.getRoom(),
                                  booking.getDanceClass(),
                                  booking.isActive(),
                                  booking.isShareable(),
                                  booking.getBookedFrom(),
                                  booking.getBookedTo());
    }
}

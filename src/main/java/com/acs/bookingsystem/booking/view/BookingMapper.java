package com.acs.bookingsystem.booking.view;

import com.acs.bookingsystem.booking.entity.Booking;
import com.acs.bookingsystem.booking.view.dto.BookingDetail;
import com.acs.bookingsystem.booking.view.dto.BookingSummary;
import org.springframework.stereotype.Service;

@Service
public class BookingMapper {

    public BookingDetail mapDetail(Booking booking) {
        return new BookingDetail(booking.getUid(),
                                 booking.getUser().getUid(),
                                 booking.getRoom(),
                                 booking.isActive(),
                                 booking.isShareable(),
                                 booking.getDanceClass().getClassType(),
                                 booking.getBookedFrom(),
                                 booking.getBookedTo(),
                                 booking.getTotalPrice());
    }

    public BookingSummary mapSummary(Booking booking) {
        return new BookingSummary(booking.getUid(),
                                  booking.getRoom(),
                                  booking.getDanceClass().getClassType(),
                                  booking.isActive(),
                                  booking.isShareable(),
                                  booking.getBookedFrom(),
                                  booking.getBookedTo());
    }
}

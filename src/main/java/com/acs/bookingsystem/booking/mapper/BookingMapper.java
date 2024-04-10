package com.acs.bookingsystem.booking.mapper;

import com.acs.bookingsystem.booking.dto.BookingDTO;
import com.acs.bookingsystem.booking.entities.Booking;

import org.springframework.stereotype.Service;

@Service
public class BookingMapper {
    public BookingDTO mapBookingToDTO(Booking booking) {
        return new BookingDTO(booking.getId(),
                              booking.getUser().getId(),
                              booking.getRoom(),
                              booking.getDanceClass()
                                     .getId(),
                              booking.getBookedFrom(),
                              booking.getBookedTo(),
                              booking.getTotalPrice());
    }

}

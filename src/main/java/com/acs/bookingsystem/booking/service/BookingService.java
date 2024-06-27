package com.acs.bookingsystem.booking.service;


import com.acs.bookingsystem.booking.dto.BookingDTO;
import com.acs.bookingsystem.booking.request.BookingRequest;
import com.acs.bookingsystem.booking.enums.Room;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {
    BookingDTO createBooking(BookingRequest bookingRequest);
    BookingDTO addUserToBooking(int userId, int bookingId);
    BookingDTO getBookingById(int bookingId);
    List<BookingDTO> getAllBookingsByUser(int userId);
    List<BookingDTO> getAllByRoomAndBetweenTwoDates(Room room, LocalDateTime dateFrom, LocalDateTime dateTo);
    void deactivateBooking(int bookingId);
}

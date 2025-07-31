package com.acs.bookingsystem.booking.service;

import com.acs.bookingsystem.booking.entity.Booking;
import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.booking.repository.BookingRepository;
import com.acs.bookingsystem.common.exception.NotFoundException;
import com.acs.bookingsystem.common.exception.model.ErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@AllArgsConstructor
public class BookingQueryService {

    BookingRepository bookingRepository;

    public Booking getBookingById(int bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Could not find booking with ID " + bookingId,
                                                         ErrorCode.INVALID_BOOKING_ID));
    }

    public Booking getOwnBookingById(int bookingId, int userId) {
        return bookingRepository.findByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new NotFoundException("Could not find booking with ID " + bookingId + " for user with ID " + userId,
                                                         ErrorCode.INVALID_BOOKING_ID));
    }

    public Page<Booking> getAllBookingsByUserId(int userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("bookedFrom").descending());

        Page<Booking> pages = bookingRepository.findAllByUserId(userId, pageable);

        return new PageImpl<>(pages.getContent(), pageable, pages.getTotalElements());
    }

    public List<Booking> getBookingsByRoomAndDates(Room room, LocalDateTime dateFrom, LocalDateTime dateTo) {
        return bookingRepository.findActiveBookingsForRoomAndTimeRange(room, null, dateFrom, dateTo);
    }

}

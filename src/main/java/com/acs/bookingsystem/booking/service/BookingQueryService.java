package com.acs.bookingsystem.booking.service;

import com.acs.bookingsystem.booking.entity.Booking;
import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.booking.repository.BookingRepository;
import com.acs.bookingsystem.common.exception.NotFoundException;
import com.acs.bookingsystem.common.exception.model.ErrorCode;
import com.acs.bookingsystem.user.enums.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;

public class BookingQueryService {

    // TODO: add logging
    public static final Logger LOG = LoggerFactory.getLogger(BookingQueryService.class);

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

        // TODO: add default week date.

        Pageable pageable = PageRequest.of(page, size, Sort.by("bookedFrom").descending());

        Page<Booking> bookingPage = bookingRepository.findAllByUserId(userId, pageable);

        return new PageImpl<>(bookingPage.getContent(), pageable, bookingPage.getTotalElements());
    }

    public List<Booking> getAllByRoomAndBetweenTwoDates(Role role, Room room, LocalDateTime dateFrom, LocalDateTime dateTo) {
        return bookingRepository.findActiveBookingsByRoomAndEndOrStartBetweenTimeRange(room, null, dateFrom, dateTo);
    }

}

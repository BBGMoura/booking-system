package com.acs.bookingsystem.booking.service;

import com.acs.bookingsystem.booking.entity.Booking;
import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.booking.repository.BookingRepository;
import com.acs.bookingsystem.booking.request.BookingRequest;
import com.acs.bookingsystem.booking.service.validation.BookingValidator;
import com.acs.bookingsystem.common.exception.NotFoundException;
import com.acs.bookingsystem.common.exception.RequestException;
import com.acs.bookingsystem.common.exception.model.ErrorCode;
import com.acs.bookingsystem.danceclass.entity.DanceClass;
import com.acs.bookingsystem.danceclass.service.DanceClassService;
import com.acs.bookingsystem.payment.PriceCalculator;
import com.acs.bookingsystem.user.entity.User;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class BookingService {

    private static final Logger LOG = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final BookingValidator bookingValidator;
    private final DanceClassService danceClassService;

    public Booking getBookingByUid(UUID uid) {
        return bookingRepository.findByUid(uid)
                .orElseThrow(() -> new NotFoundException(
                        "Could not find booking " + uid,
                        ErrorCode.INVALID_BOOKING_ID));
    }

    public Booking getBookingByUidAndUser(UUID uid, int userId) {
        return bookingRepository.findByUidAndUserId(uid, userId)
                .orElseThrow(() -> new NotFoundException(
                        "Could not find booking " + uid,
                        ErrorCode.INVALID_BOOKING_ID));
    }

    public Page<Booking> getAllBookingsByUserId(int userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("bookedFrom").descending());
        return bookingRepository.findAllByUserId(userId, pageable);
    }

    public List<Booking> getBookingsByRoomAndDates(Room room, LocalDateTime dateFrom, LocalDateTime dateTo) {
        return bookingRepository.findActiveBookingsForRoomAndTimeRange(room, null, dateFrom, dateTo);
    }

    @Transactional
    public Booking createBooking(BookingRequest bookingRequest, User user) {
        LOG.debug("Creating booking for user {}", user.getId());

        DanceClass danceClass = danceClassService.getActiveDanceClass(bookingRequest.classType(), user.getRole());

        bookingValidator.validate(bookingRequest)
                        .ifPresent(f -> { throw new RequestException(f.message(), f.code()); });

        BigDecimal totalCost = PriceCalculator.calculateTotalPrice(bookingRequest.dateFrom(), bookingRequest.dateTo(), danceClass);

        Booking saved = bookingRepository.save(Booking.builder()
                                                      .user(user)
                                                      .room(bookingRequest.room())
                                                      .danceClass(danceClass)
                                                      .active(true)
                                                      .shareable(bookingRequest.isShareable())
                                                      .bookedFrom(bookingRequest.dateFrom())
                                                      .bookedTo(bookingRequest.dateTo())
                                                      .totalPrice(totalCost)
                                                      .build());
        LOG.debug("Created booking {}", saved.getUid());
        return saved;
    }

    @Transactional
    public void deactivateBookingByUserId(UUID bookingUid, int userId) {
        Booking booking = getBookingByUidAndUser(bookingUid, userId);
        booking.deactivate();
        bookingRepository.save(booking);
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deactivateBooking(UUID bookingUid) {
        Booking booking = getBookingByUid(bookingUid);
        booking.deactivate();
        bookingRepository.save(booking);
    }

    @Transactional
    public void deactivateAllBookingsByUserId(int userId) {
        LOG.debug("Deactivating all bookings for user {}", userId);
        int count = bookingRepository.deactivateBookingsByUserId(userId);
        LOG.debug("Deactivated {} bookings for user {}", count, userId);
    }
}

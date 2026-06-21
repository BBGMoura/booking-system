package com.acs.bookingsystem.booking.service;

import com.acs.bookingsystem.booking.BookingWithStatus;
import com.acs.bookingsystem.booking.entity.Booking;
import com.acs.bookingsystem.booking.enums.BookingStatusType;
import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.booking.repository.AdvisoryLockRepository;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class BookingService {

  private final BookingRepository bookingRepository;
  private final BookingStatusService bookingStatusService;
  private final BookingValidator bookingValidator;
  private final DanceClassService danceClassService;
  private final AdvisoryLockRepository advisoryLockRepository;

  public BookingWithStatus getBookingByUid(UUID uid) {
    Booking booking = fetchBookingByUid(uid);
    return new BookingWithStatus(booking, bookingStatusService.resolveStatus(booking));
  }

  public BookingWithStatus getBookingByUidAndUser(UUID uid, Long userId) {
    Booking booking = fetchBookingByUidAndUser(uid, userId);
    return new BookingWithStatus(booking, bookingStatusService.resolveStatus(booking));
  }

  public Page<BookingWithStatus> getAllBookingsByUserId(Long userId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("bookedFrom").descending());
    return bookingRepository
        .findAllByUserId(userId, pageable)
        .map(b -> new BookingWithStatus(b, bookingStatusService.resolveStatus(b)));
  }

  public Page<BookingWithStatus> getAllBookingsByUserUid(UUID userUid, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("bookedFrom").descending());
    return bookingRepository
        .findAllByUserUid(userUid, pageable)
        .map(b -> new BookingWithStatus(b, bookingStatusService.resolveStatus(b)));
  }

  public List<BookingWithStatus> getBookingsByRoomAndDates(
      Room room, LocalDateTime dateFrom, LocalDateTime dateTo) {
    return bookingRepository
        .findActiveBookingsForRoomAndTimeRange(room, null, dateFrom, dateTo)
        .stream()
        .map(b -> new BookingWithStatus(b, bookingStatusService.resolveStatus(b)))
        .toList();
  }

  @Transactional
  public BookingWithStatus createBooking(BookingRequest bookingRequest, User user) {
    log.info("Creating booking for user with uid {}", user.getUid());

    DanceClass danceClass =
        danceClassService.getActiveDanceClass(bookingRequest.classType(), user.getRole());

    advisoryLockRepository.acquireRoomLock(bookingRequest.room());

    bookingValidator
        .validate(bookingRequest)
        .ifPresent(
            f -> {
              throw new RequestException(f.message(), f.code());
            });

    BigDecimal totalCost =
        PriceCalculator.calculateTotalPrice(
            bookingRequest.dateFrom(), bookingRequest.dateTo(), danceClass);

    Booking booking =
        bookingRepository.save(
            Booking.builder()
                .user(user)
                .room(bookingRequest.room())
                .danceClass(danceClass)
                .shareable(bookingRequest.isShareable())
                .bookedFrom(bookingRequest.dateFrom())
                .bookedTo(bookingRequest.dateTo())
                .totalPrice(totalCost)
                .build());

    bookingStatusService.save(booking, user, BookingStatusType.BOOKED);
    log.info("Created booking {}", booking.getUid());
    return new BookingWithStatus(booking, BookingStatusType.BOOKED);
  }

  @Transactional
  public void cancelBookingByUserId(UUID bookingUid, Long userId) {
    Booking booking = fetchBookingByUidAndUser(bookingUid, userId);
    bookingStatusService.save(booking, booking.getUser(), BookingStatusType.CANCELLED);
  }

  @Transactional
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public void cancelBooking(UUID bookingUid, User admin) {
    Booking booking = fetchBookingByUid(bookingUid);
    bookingStatusService.save(booking, admin, BookingStatusType.CANCELLED);
  }

  @Transactional
  public void cancelAllBookingsByUserId(Long userId, User cancelledBy) {
    log.debug("Cancelling all bookings for user {}", userId);
    List<Booking> activeBookings = bookingRepository.findActiveBookingsByUserId(userId);
    activeBookings.forEach(
        b -> bookingStatusService.save(b, cancelledBy, BookingStatusType.CANCELLED));
    log.debug("Cancelled {} bookings for user {}", activeBookings.size(), userId);
  }

  private Booking fetchBookingByUid(UUID uid) {
    return bookingRepository
        .findByUid(uid)
        .orElseThrow(
            () ->
                new NotFoundException(
                    "Could not find booking " + uid, ErrorCode.INVALID_BOOKING_ID));
  }

  private Booking fetchBookingByUidAndUser(UUID uid, Long userId) {
    return bookingRepository
        .findByUidAndUserId(uid, userId)
        .orElseThrow(
            () ->
                new NotFoundException(
                    "Could not find booking " + uid, ErrorCode.INVALID_BOOKING_ID));
  }
}

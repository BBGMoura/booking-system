package com.acs.bookingsystem.booking.service;

import com.acs.bookingsystem.booking.BookingWithStatus;
import com.acs.bookingsystem.booking.entity.Booking;
import com.acs.bookingsystem.booking.enums.BookingStatusType;
import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.booking.repository.AdvisoryLockRepository;
import com.acs.bookingsystem.booking.repository.BookingRepository;
import com.acs.bookingsystem.booking.repository.BookingSpecifications;
import com.acs.bookingsystem.booking.request.BookingRequest;
import com.acs.bookingsystem.booking.service.validation.BookingValidator;
import com.acs.bookingsystem.common.exception.AuthorizationException;
import com.acs.bookingsystem.common.exception.NotFoundException;
import com.acs.bookingsystem.common.exception.RequestException;
import com.acs.bookingsystem.common.exception.model.ErrorCode;
import com.acs.bookingsystem.danceclass.entity.DanceClass;
import com.acs.bookingsystem.danceclass.enums.ClassType;
import com.acs.bookingsystem.danceclass.service.DanceClassService;
import com.acs.bookingsystem.payment.PriceCalculator;
import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.enums.Role;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
    return getAllBookingsByUserId(userId, page, size, null, null, null, null);
  }

  /**
   * @param dateFrom/dateTo each independently optional: bookings starting at/after dateFrom and/or
   *     before dateTo. Checked against bookedFrom alone — bookings can never span more than one day
   *     (BookingTimeValidator enforces same-day start/end), so no overlap logic is needed here.
   * @param order sort direction on bookedFrom; defaults to descending (today's existing behavior)
   *     when not given
   */
  public Page<BookingWithStatus> getAllBookingsByUserId(
      Long userId,
      int page,
      int size,
      BookingStatusType status,
      OffsetDateTime dateFrom,
      OffsetDateTime dateTo,
      Sort.Direction order) {
    return findFiltered(
        BookingSpecifications.forUser(userId), page, size, status, dateFrom, dateTo, order);
  }

  /** Same filters and sorting as {@link #getAllBookingsByUserId}, for the user with this uid. */
  public Page<BookingWithStatus> getAllBookingsByUserUid(
      UUID userUid,
      int page,
      int size,
      BookingStatusType status,
      OffsetDateTime dateFrom,
      OffsetDateTime dateTo,
      Sort.Direction order) {
    return findFiltered(
        BookingSpecifications.forUserUid(userUid), page, size, status, dateFrom, dateTo, order);
  }

  private Page<BookingWithStatus> findFiltered(
      Specification<Booking> userSpec,
      int page,
      int size,
      BookingStatusType status,
      OffsetDateTime dateFrom,
      OffsetDateTime dateTo,
      Sort.Direction order) {
    Sort.Direction direction = order != null ? order : Sort.Direction.DESC;
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "bookedFrom"));

    Specification<Booking> spec =
        Specification.where(userSpec)
            .and(BookingSpecifications.hasLatestStatus(status))
            .and(BookingSpecifications.inRange(dateFrom, dateTo));

    return bookingRepository
        .findAll(spec, pageable)
        .map(b -> new BookingWithStatus(b, bookingStatusService.resolveStatus(b)));
  }

  public List<BookingWithStatus> getBookingsByRoomAndDates(
      Room room, OffsetDateTime dateFrom, OffsetDateTime dateTo) {
    return bookingRepository
        .findActiveBookingsForRoomAndTimeRange(room, null, dateFrom, dateTo)
        .stream()
        .map(b -> new BookingWithStatus(b, bookingStatusService.resolveStatus(b)))
        .toList();
  }

  /**
   * Runs the same checks as {@link #createBooking} without saving. Unlike createBooking it takes no
   * room lock: nothing is written, so the answer is only a snapshot and a later create can still be
   * rejected if another booking lands first.
   */
  public void validateBooking(BookingRequest bookingRequest, User user) {
    resolveFloorFee(bookingRequest, user);
    validateOrThrow(bookingRequest);
  }

  /** A booking the signed-in user makes for themselves (or, for Closed, for the studio). */
  @Transactional
  public BookingWithStatus createBooking(BookingRequest bookingRequest, User user) {
    return create(bookingRequest, user, user);
  }

  /**
   * @param bookedFor who the booking is for. Ignored for studio use, which is never for a person.
   * @param createdBy who is making the booking; must be an admin to book Closed.
   */
  @Transactional
  public BookingWithStatus createBooking(
      BookingRequest bookingRequest, User bookedFor, User createdBy) {
    return create(bookingRequest, bookedFor, createdBy);
  }

  // Both public overloads call this rather than each other: a call through `this` skips the
  // transactional proxy, so only the outer method's @Transactional would apply.
  private BookingWithStatus create(BookingRequest bookingRequest, User bookedFor, User createdBy) {
    log.info("Creating booking made by user with uid {}", createdBy.getUid());

    DanceClass floorFee = resolveFloorFee(bookingRequest, bookedFor, createdBy);

    advisoryLockRepository.acquireRoomLock(bookingRequest.room());

    validateOrThrow(bookingRequest);

    boolean studioUse = bookingRequest.classType().isStudioUse();
    BigDecimal totalCost =
        studioUse
            ? BigDecimal.ZERO
            : PriceCalculator.calculateTotalPrice(
                bookingRequest.dateFrom(), bookingRequest.dateTo(), floorFee);

    Booking booking =
        bookingRepository.save(
            Booking.builder()
                .bookingType(bookingRequest.classType())
                .bookedFor(studioUse ? null : bookedFor)
                .createdBy(createdBy)
                .room(bookingRequest.room())
                .danceClass(floorFee)
                .shareable(bookingRequest.isShareable())
                .bookedFrom(bookingRequest.dateFrom())
                .bookedTo(bookingRequest.dateTo())
                .totalPrice(totalCost)
                .build());

    bookingStatusService.save(booking, createdBy, BookingStatusType.BOOKED);
    log.info("Created booking {}", booking.getUid());
    return new BookingWithStatus(booking, BookingStatusType.BOOKED);
  }

  private DanceClass resolveFloorFee(BookingRequest request, User user) {
    return resolveFloorFee(request, user, user);
  }

  /** Returns the floor fee that prices a room hire booking, or null for studio use. */
  private DanceClass resolveFloorFee(BookingRequest request, User bookedFor, User createdBy) {
    ClassType type = request.classType();
    if (type == ClassType.GROUP) {
      throw new RequestException(
          "Group classes are booked through timetables.", ErrorCode.INVALID_BOOKING_REQUEST);
    }
    if (type.isStudioUse()) {
      if (createdBy.getRole() != Role.ROLE_ADMIN) {
        throw new AuthorizationException(
            "Only admins can book the studio as Closed.", ErrorCode.INVALID_BOOKING_REQUEST);
      }
      if (request.isShareable()) {
        throw new RequestException("Closed can't be shared.", ErrorCode.INVALID_BOOKING_REQUEST);
      }
      return null;
    }
    return danceClassService.getActiveDanceClass(type, bookedFor.getRole());
  }

  @Transactional
  public void cancelBookingByUserId(UUID bookingUid, Long userId) {
    Booking booking = fetchBookingByUidAndUser(bookingUid, userId);
    bookingStatusService.save(booking, booking.getBookedFor(), BookingStatusType.CANCELLED);
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

  private void validateOrThrow(BookingRequest bookingRequest) {
    bookingValidator
        .validate(bookingRequest)
        .ifPresent(
            f -> {
              throw new RequestException(f.message(), f.code());
            });
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
        .findByUidAndBookedForId(uid, userId)
        .orElseThrow(
            () ->
                new NotFoundException(
                    "Could not find booking " + uid, ErrorCode.INVALID_BOOKING_ID));
  }
}

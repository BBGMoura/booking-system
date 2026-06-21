package com.acs.bookingsystem.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.acs.bookingsystem.booking.BookingWithStatus;
import com.acs.bookingsystem.booking.entity.Booking;
import com.acs.bookingsystem.booking.enums.BookingStatusType;
import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.booking.repository.AdvisoryLockRepository;
import com.acs.bookingsystem.booking.repository.BookingRepository;
import com.acs.bookingsystem.booking.request.BookingRequest;
import com.acs.bookingsystem.booking.service.validation.BookingValidator;
import com.acs.bookingsystem.booking.service.validation.ValidationFailure;
import com.acs.bookingsystem.common.exception.LockTimeoutException;
import com.acs.bookingsystem.common.exception.NotFoundException;
import com.acs.bookingsystem.common.exception.RequestException;
import com.acs.bookingsystem.common.exception.model.ErrorCode;
import com.acs.bookingsystem.danceclass.entity.DanceClass;
import com.acs.bookingsystem.danceclass.enums.ClassType;
import com.acs.bookingsystem.danceclass.service.DanceClassService;
import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.enums.Role;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

  @Mock private BookingRepository bookingRepository;
  @Mock private BookingStatusService bookingStatusService;
  @Mock private BookingValidator bookingValidator;
  @Mock private DanceClassService danceClassService;
  @Mock private AdvisoryLockRepository advisoryLockRepository;

  @InjectMocks private BookingService bookingService;

  private static final UUID BOOKING_UID = UUID.randomUUID();
  private static final Long USER_ID = 1L;

  private final User user =
      User.builder()
          .id(USER_ID)
          .uid(UUID.randomUUID())
          .email("test@example.com")
          .role(Role.ROLE_USER)
          .build();

  private final User admin =
      User.builder()
          .id(99L)
          .uid(UUID.randomUUID())
          .email("admin@example.com")
          .role(Role.ROLE_ADMIN)
          .build();

  private final DanceClass danceClass =
      new DanceClass(1L, ClassType.PRIVATE, true, BigDecimal.TEN, Role.ROLE_USER);

  private final BookingRequest request =
      new BookingRequest(
          Room.ASTAIRE,
          ClassType.PRIVATE,
          false,
          LocalDateTime.of(2025, 6, 2, 10, 0),
          LocalDateTime.of(2025, 6, 2, 11, 0));

  private final Booking booking = Booking.builder().uid(BOOKING_UID).user(user).build();

  // --- createBooking ---

  @Test
  void givenValidRequest_whenCreateBooking_thenSavesAndReturnsBookingWithBookedStatus() {
    when(danceClassService.getActiveDanceClass(ClassType.PRIVATE, Role.ROLE_USER))
        .thenReturn(danceClass);
    when(bookingValidator.validate(request)).thenReturn(Optional.empty());
    when(bookingRepository.save(any())).thenReturn(booking);

    var result = bookingService.createBooking(request, user);

    assertThat(result.booking()).isEqualTo(booking);
    assertThat(result.status()).isEqualTo(BookingStatusType.BOOKED);
    verify(bookingRepository).save(any(Booking.class));
  }

  @Test
  void givenValidRequest_whenCreateBooking_thenInsertsBookedStatus() {
    when(danceClassService.getActiveDanceClass(ClassType.PRIVATE, Role.ROLE_USER))
        .thenReturn(danceClass);
    when(bookingValidator.validate(request)).thenReturn(Optional.empty());
    when(bookingRepository.save(any())).thenReturn(booking);

    bookingService.createBooking(request, user);

    verify(bookingStatusService).save(eq(booking), eq(user), eq(BookingStatusType.BOOKED));
  }

  @Test
  void givenValidRequest_whenCreateBooking_thenAcquiresLockForCorrectRoom() {
    when(danceClassService.getActiveDanceClass(ClassType.PRIVATE, Role.ROLE_USER))
        .thenReturn(danceClass);
    when(bookingValidator.validate(request)).thenReturn(Optional.empty());
    when(bookingRepository.save(any())).thenReturn(booking);

    bookingService.createBooking(request, user);

    verify(advisoryLockRepository).acquireRoomLock(Room.ASTAIRE);
  }

  @Test
  void givenLockTimeout_whenCreateBooking_thenThrowsLockTimeoutExceptionAndDoesNotSave() {
    when(danceClassService.getActiveDanceClass(ClassType.PRIVATE, Role.ROLE_USER))
        .thenReturn(danceClass);
    doThrow(
            new LockTimeoutException(
                ErrorCode.BOOKING_LOCK_TIMEOUT.getDescription(),
                new RuntimeException(),
                ErrorCode.BOOKING_LOCK_TIMEOUT))
        .when(advisoryLockRepository)
        .acquireRoomLock(Room.ASTAIRE);

    assertThatThrownBy(() -> bookingService.createBooking(request, user))
        .isInstanceOf(LockTimeoutException.class);

    verify(bookingRepository, never()).save(any());
    verify(bookingStatusService, never()).save(any(), any(), any());
  }

  @Test
  void givenValidationFailure_whenCreateBooking_thenThrowsRequestExceptionAndDoesNotSave() {
    when(danceClassService.getActiveDanceClass(ClassType.PRIVATE, Role.ROLE_USER))
        .thenReturn(danceClass);
    when(bookingValidator.validate(request))
        .thenReturn(
            Optional.of(new ValidationFailure("Time invalid", ErrorCode.BOOKING_TIME_INVALID)));

    assertThatThrownBy(() -> bookingService.createBooking(request, user))
        .isInstanceOf(RequestException.class)
        .hasMessageContaining("Time invalid");

    verify(bookingRepository, never()).save(any());
    verify(bookingStatusService, never()).save(any(), any(), any());
  }

  // --- cancelBookingByUserId ---

  @Test
  void
      givenValidUidAndUser_whenCancelBookingByUserId_thenInsertsCancelledStatusWithUserAsCreatedBy() {
    when(bookingRepository.findByUidAndUserId(BOOKING_UID, USER_ID))
        .thenReturn(Optional.of(booking));

    bookingService.cancelBookingByUserId(BOOKING_UID, USER_ID);

    verify(bookingStatusService).save(eq(booking), eq(user), eq(BookingStatusType.CANCELLED));
  }

  @Test
  void givenBookingNotOwnedByUser_whenCancelBookingByUserId_thenThrowsNotFoundException() {
    when(bookingRepository.findByUidAndUserId(BOOKING_UID, USER_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> bookingService.cancelBookingByUserId(BOOKING_UID, USER_ID))
        .isInstanceOf(NotFoundException.class);

    verify(bookingStatusService, never()).save(any(), any(), any());
  }

  // --- cancelBooking (admin) ---

  @Test
  void givenValidUid_whenAdminCancelsBooking_thenInsertsCancelledStatusWithAdminAsCreatedBy() {
    when(bookingRepository.findByUid(BOOKING_UID)).thenReturn(Optional.of(booking));

    bookingService.cancelBooking(BOOKING_UID, admin);

    verify(bookingStatusService).save(eq(booking), eq(admin), eq(BookingStatusType.CANCELLED));
  }

  @Test
  void givenUnknownUid_whenAdminCancelsBooking_thenThrowsNotFoundException() {
    when(bookingRepository.findByUid(BOOKING_UID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> bookingService.cancelBooking(BOOKING_UID, user))
        .isInstanceOf(NotFoundException.class);

    verify(bookingStatusService, never()).save(any(), any(), any());
  }

  // --- cancelAllBookingsByUserId ---

  @Test
  void
      givenUserId_whenCancelAllBookings_thenInsertsCancelledForEachActiveBookingWithAdminAsCreatedBy() {
    Booking booking2 = Booking.builder().uid(UUID.randomUUID()).user(user).build();
    when(bookingRepository.findActiveBookingsByUserId(USER_ID))
        .thenReturn(List.of(booking, booking2));

    bookingService.cancelAllBookingsByUserId(USER_ID, admin);

    verify(bookingStatusService).save(eq(booking), eq(admin), eq(BookingStatusType.CANCELLED));
    verify(bookingStatusService).save(eq(booking2), eq(admin), eq(BookingStatusType.CANCELLED));
  }

  // --- getBookingByUid ---

  @Test
  void givenValidUid_whenGetBookingByUid_thenReturnsBookingWithStatus() {
    when(bookingRepository.findByUid(BOOKING_UID)).thenReturn(Optional.of(booking));
    when(bookingStatusService.resolveStatus(booking)).thenReturn(BookingStatusType.BOOKED);

    BookingWithStatus result = bookingService.getBookingByUid(BOOKING_UID);

    assertThat(result.booking().getUid()).isEqualTo(BOOKING_UID);
    assertThat(result.status()).isEqualTo(BookingStatusType.BOOKED);
  }

  @Test
  void givenUnknownUid_whenGetBookingByUid_thenThrowsNotFoundException() {
    when(bookingRepository.findByUid(BOOKING_UID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> bookingService.getBookingByUid(BOOKING_UID))
        .isInstanceOf(NotFoundException.class);
  }

  // --- getBookingByUidAndUser ---

  @Test
  void givenValidUidAndOwner_whenGetBookingByUidAndUser_thenReturnsBookingWithStatus() {
    when(bookingRepository.findByUidAndUserId(BOOKING_UID, USER_ID))
        .thenReturn(Optional.of(booking));
    when(bookingStatusService.resolveStatus(booking)).thenReturn(BookingStatusType.BOOKED);

    BookingWithStatus result = bookingService.getBookingByUidAndUser(BOOKING_UID, USER_ID);

    assertThat(result.booking().getUid()).isEqualTo(BOOKING_UID);
    assertThat(result.status()).isEqualTo(BookingStatusType.BOOKED);
  }

  @Test
  void givenUidNotOwnedByUser_whenGetBookingByUidAndUser_thenThrowsNotFoundException() {
    when(bookingRepository.findByUidAndUserId(BOOKING_UID, USER_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> bookingService.getBookingByUidAndUser(BOOKING_UID, USER_ID))
        .isInstanceOf(NotFoundException.class);
  }

  // --- getAllBookingsByUserId ---

  @Test
  void givenUserId_whenGetAllBookingsByUserId_thenReturnsPaginatedResults() {
    Page<Booking> page = new PageImpl<>(List.of(booking));
    when(bookingRepository.findAllByUserId(eq(USER_ID), any(Pageable.class))).thenReturn(page);
    when(bookingStatusService.resolveStatus(booking)).thenReturn(BookingStatusType.BOOKED);

    Page<BookingWithStatus> result = bookingService.getAllBookingsByUserId(USER_ID, 0, 5);

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).status()).isEqualTo(BookingStatusType.BOOKED);
  }

  // --- getAllBookingsByUserUid ---

  @Test
  void givenUserUid_whenGetAllBookingsByUserUid_thenReturnsPaginatedResultsSortedByBookedFrom() {
    UUID userUid = UUID.randomUUID();
    Page<Booking> page = new PageImpl<>(List.of(booking));
    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    when(bookingRepository.findAllByUserUid(eq(userUid), pageableCaptor.capture()))
        .thenReturn(page);
    when(bookingStatusService.resolveStatus(booking)).thenReturn(BookingStatusType.BOOKED);

    Page<BookingWithStatus> result = bookingService.getAllBookingsByUserUid(userUid, 0, 5);

    assertThat(result.getContent()).hasSize(1);
    Sort.Order order = pageableCaptor.getValue().getSort().getOrderFor("bookedFrom");
    assertThat(order).isNotNull();
    assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
  }

  // --- getBookingsByRoomAndDates ---

  @Test
  void givenRoomAndDates_whenGetBookingsByRoomAndDates_thenReturnsBookings() {
    LocalDateTime from = LocalDateTime.of(2025, 6, 2, 10, 0);
    LocalDateTime to = LocalDateTime.of(2025, 6, 2, 11, 0);
    when(bookingRepository.findActiveBookingsForRoomAndTimeRange(
            eq(Room.ASTAIRE), eq(null), eq(from), eq(to)))
        .thenReturn(List.of(booking));
    when(bookingStatusService.resolveStatus(booking)).thenReturn(BookingStatusType.BOOKED);

    List<BookingWithStatus> result =
        bookingService.getBookingsByRoomAndDates(Room.ASTAIRE, from, to);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).status()).isEqualTo(BookingStatusType.BOOKED);
  }
}

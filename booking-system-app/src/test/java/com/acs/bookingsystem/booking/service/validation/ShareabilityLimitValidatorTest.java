package com.acs.bookingsystem.booking.service.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.acs.bookingsystem.booking.entity.Booking;
import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.booking.repository.BookingRepository;
import com.acs.bookingsystem.booking.request.BookingRequest;
import com.acs.bookingsystem.common.exception.model.ErrorCode;
import com.acs.bookingsystem.danceclass.enums.ClassType;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShareabilityLimitValidatorTest {

  @Mock private BookingRepository bookingRepository;

  private ShareabilityLimitValidator validator;

  private static final OffsetDateTime START =
      OffsetDateTime.of(2025, 6, 2, 10, 0, 0, 0, ZoneOffset.UTC);
  private static final OffsetDateTime END =
      OffsetDateTime.of(2025, 6, 2, 11, 0, 0, 0, ZoneOffset.UTC);

  @BeforeEach
  void setup() {
    validator = new ShareabilityLimitValidator(bookingRepository);
  }

  @Test
  void givenShareableRequestWithNoExistingBookings_shouldPass() {
    when(bookingRepository.findActiveBookingsForRoomAndTimeRange(
            eq(Room.ASTAIRE), eq(true), any(), any()))
        .thenReturn(List.of());

    Optional<ValidationFailure> result = validator.validate(shareableRequest());
    assertThat(result).isEmpty();
  }

  @Test
  void givenNonShareableRequestWithExistingShareableBookings_shouldFail() {
    when(bookingRepository.findActiveBookingsForRoomAndTimeRange(
            eq(Room.ASTAIRE), eq(true), any(), any()))
        .thenReturn(List.of(shareableBooking(START, END)));

    Optional<ValidationFailure> result = validator.validate(nonShareableRequest());
    assertThat(result).isPresent();
    assertThat(result.get().code()).isEqualTo(ErrorCode.BOOKING_CONFLICT);
    assertThat(result.get().message()).contains("non-shareable");
  }

  @Test
  void givenShareableRequestUnderLimit_shouldPass() {
    List<Booking> twoBookings = List.of(shareableBooking(START, END), shareableBooking(START, END));
    when(bookingRepository.findActiveBookingsForRoomAndTimeRange(
            eq(Room.ASTAIRE), eq(true), any(), any()))
        .thenReturn(twoBookings);

    Optional<ValidationFailure> result = validator.validate(shareableRequest());
    assertThat(result).isEmpty();
  }

  @Test
  void givenShareableRequestAtMaximumSlots_shouldFail() {
    List<Booking> threeBookings =
        List.of(
            shareableBooking(START, END),
            shareableBooking(START, END),
            shareableBooking(START, END));
    when(bookingRepository.findActiveBookingsForRoomAndTimeRange(
            eq(Room.ASTAIRE), eq(true), any(), any()))
        .thenReturn(threeBookings);

    Optional<ValidationFailure> result = validator.validate(shareableRequest());
    assertThat(result).isPresent();
    assertThat(result.get().code()).isEqualTo(ErrorCode.BOOKING_SHAREABLE_LIMIT);
    assertThat(result.get().message()).contains("3");
  }

  @Test
  void givenShareableRequestWithStaggeredBookingsConvergingAtLimit_shouldFail() {
    // A:[10:00-10:20], B:[10:10-10:30], C:[10:15-10:35] — all three active at slot [10:15-10:20]
    List<Booking> staggered =
        List.of(
            shareableBooking(START, START.plusMinutes(20)),
            shareableBooking(START.plusMinutes(10), START.plusMinutes(30)),
            shareableBooking(START.plusMinutes(15), START.plusMinutes(35)));
    when(bookingRepository.findActiveBookingsForRoomAndTimeRange(
            eq(Room.ASTAIRE), eq(true), any(), any()))
        .thenReturn(staggered);

    Optional<ValidationFailure> result = validator.validate(shareableRequest());
    assertThat(result).isPresent();
    assertThat(result.get().code()).isEqualTo(ErrorCode.BOOKING_SHAREABLE_LIMIT);
  }

  @Test
  void givenShareableRequestWithStaggeredBookingsNeverConverging_shouldPass() {
    // A:[10:00-10:20], B:[10:25-10:45], C:[10:50-11:10] — never more than 1 active per slot
    List<Booking> staggered =
        List.of(
            shareableBooking(START, START.plusMinutes(20)),
            shareableBooking(START.plusMinutes(25), START.plusMinutes(45)),
            shareableBooking(START.plusMinutes(50), END.plusMinutes(10)));
    when(bookingRepository.findActiveBookingsForRoomAndTimeRange(
            eq(Room.ASTAIRE), eq(true), any(), any()))
        .thenReturn(staggered);

    Optional<ValidationFailure> result = validator.validate(shareableRequest());
    assertThat(result).isEmpty();
  }

  private BookingRequest shareableRequest() {
    return new BookingRequest(Room.ASTAIRE, ClassType.PRACTICE, true, START, END);
  }

  private BookingRequest nonShareableRequest() {
    return new BookingRequest(Room.ASTAIRE, ClassType.PRIVATE, false, START, END);
  }

  private Booking shareableBooking(OffsetDateTime from, OffsetDateTime to) {
    return Booking.builder()
        .room(Room.ASTAIRE)
        .shareable(true)
        .bookedFrom(from)
        .bookedTo(to)
        .build();
  }
}

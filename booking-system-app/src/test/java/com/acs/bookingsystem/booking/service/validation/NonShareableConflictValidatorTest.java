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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NonShareableConflictValidatorTest {

  @Mock private BookingRepository bookingRepository;

  private NonShareableConflictValidator validator;

  private static final LocalDateTime START = LocalDateTime.of(2025, 6, 2, 10, 0);
  private static final LocalDateTime END = LocalDateTime.of(2025, 6, 2, 11, 0);

  @BeforeEach
  void setup() {
    validator = new NonShareableConflictValidator(bookingRepository);
  }

  @Test
  void givenNoConflictingBookings_shouldReturnEmpty() {
    when(bookingRepository.findActiveBookingsForRoomAndTimeRange(
            eq(Room.ASTAIRE), eq(false), any(), any()))
        .thenReturn(List.of());

    Optional<ValidationFailure> result = validator.validate(nonShareableRequest());
    assertThat(result).isEmpty();
  }

  @Test
  void givenConflictingNonShareableBooking_shouldFail() {
    when(bookingRepository.findActiveBookingsForRoomAndTimeRange(
            eq(Room.ASTAIRE), eq(false), any(), any()))
        .thenReturn(List.of(new Booking()));

    Optional<ValidationFailure> result = validator.validate(nonShareableRequest());
    assertThat(result).isPresent();
    assertThat(result.get().code()).isEqualTo(ErrorCode.BOOKING_CONFLICT);
    assertThat(result.get().message()).contains("unavailable");
  }

  @Test
  void givenShareableRequestWithExistingNonShareableBooking_shouldFail() {
    when(bookingRepository.findActiveBookingsForRoomAndTimeRange(
            eq(Room.ASTAIRE), eq(false), any(), any()))
        .thenReturn(List.of(new Booking()));

    Optional<ValidationFailure> result = validator.validate(shareableRequest());
    assertThat(result).isPresent();
    assertThat(result.get().code()).isEqualTo(ErrorCode.BOOKING_CONFLICT);
    assertThat(result.get().message()).contains("unavailable");
  }

  private BookingRequest nonShareableRequest() {
    return new BookingRequest(Room.ASTAIRE, ClassType.PRIVATE, false, START, END);
  }

  private BookingRequest shareableRequest() {
    return new BookingRequest(Room.ASTAIRE, ClassType.PRACTICE, true, START, END);
  }
}

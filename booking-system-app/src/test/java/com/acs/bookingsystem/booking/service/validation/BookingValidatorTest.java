package com.acs.bookingsystem.booking.service.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.booking.repository.BookingRepository;
import com.acs.bookingsystem.booking.request.BookingRequest;
import com.acs.bookingsystem.common.exception.model.ErrorCode;
import com.acs.bookingsystem.danceclass.enums.ClassType;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookingValidatorTest {

  @Mock private BookingRepository bookingRepository;

  private BookingValidator validator;

  private static final LocalDateTime VALID_START = LocalDateTime.of(2025, 6, 2, 10, 0);
  private static final LocalDateTime VALID_END = LocalDateTime.of(2025, 6, 2, 11, 0);

  @BeforeEach
  void setup() {
    ScheduleProperties props = new ScheduleProperties();

    ScheduleProperties.DayTime weekday = new ScheduleProperties.DayTime();
    weekday.setOpening(LocalTime.of(9, 0));
    weekday.setClosing(LocalTime.of(22, 0));

    ScheduleProperties.DayTime saturday = new ScheduleProperties.DayTime();
    saturday.setOpening(LocalTime.of(9, 0));
    saturday.setClosing(LocalTime.of(18, 0));

    ScheduleProperties.DayTime sunday = new ScheduleProperties.DayTime();
    sunday.setOpening(LocalTime.of(10, 0));
    sunday.setClosing(LocalTime.of(16, 0));

    props.setWeekday(weekday);
    props.setSaturday(saturday);
    props.setSunday(sunday);

    validator = new BookingValidator(props, bookingRepository);
  }

  @Test
  void givenValidBookingWithNoConflicts_shouldReturnEmpty() {
    when(bookingRepository.findActiveBookingsForRoomAndTimeRange(any(), any(), any(), any()))
        .thenReturn(List.of());

    Optional<ValidationFailure> result = validator.validate(validRequest());
    assertThat(result).isEmpty();
  }

  @Test
  void givenInvalidTime_shouldFailWithTimeErrorAndNotHitDatabase() {
    LocalDateTime invalidEnd = VALID_START.minusHours(1);

    Optional<ValidationFailure> result = validator.validate(request(VALID_START, invalidEnd));

    assertThat(result).isPresent();
    assertThat(result.get().code()).isEqualTo(ErrorCode.BOOKING_TIME_INVALID);
    verify(bookingRepository, never())
        .findActiveBookingsForRoomAndTimeRange(any(), any(), any(), any());
  }

  @Test
  void givenConflictingBooking_shouldFailWithConflictError() {
    when(bookingRepository.findActiveBookingsForRoomAndTimeRange(
            eq(Room.ASTAIRE), eq(false), any(), any()))
        .thenReturn(List.of(new com.acs.bookingsystem.booking.entity.Booking()));

    Optional<ValidationFailure> result = validator.validate(validRequest());

    assertThat(result).isPresent();
    assertThat(result.get().code()).isEqualTo(ErrorCode.BOOKING_CONFLICT);
  }

  private BookingRequest validRequest() {
    return request(VALID_START, VALID_END);
  }

  private BookingRequest request(LocalDateTime from, LocalDateTime to) {
    return new BookingRequest(Room.ASTAIRE, ClassType.PRIVATE, false, from, to);
  }
}

package com.acs.bookingsystem.booking.service.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.booking.request.BookingRequest;
import com.acs.bookingsystem.danceclass.enums.ClassType;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BookingTimeValidatorTest {

  private BookingTimeValidator validator;

  // Opening hours: weekday 09:00-22:00, Saturday 09:00-18:00, Sunday 10:00-16:00
  private static final LocalTime WEEKDAY_OPEN = LocalTime.of(9, 0);
  private static final LocalTime WEEKDAY_CLOSE = LocalTime.of(22, 0);
  private static final LocalTime SAT_OPEN = LocalTime.of(9, 0);
  private static final LocalTime SAT_CLOSE = LocalTime.of(18, 0);
  private static final LocalTime SUN_OPEN = LocalTime.of(10, 0);
  private static final LocalTime SUN_CLOSE = LocalTime.of(16, 0);

  // A valid Monday slot: 10:00–11:00
  private static final LocalDateTime VALID_START = LocalDateTime.of(2025, 6, 2, 10, 0); // Monday
  private static final LocalDateTime VALID_END = LocalDateTime.of(2025, 6, 2, 11, 0);

  @BeforeEach
  void setup() {
    ScheduleProperties props = new ScheduleProperties();

    ScheduleProperties.DayTime weekday = new ScheduleProperties.DayTime();
    weekday.setOpening(WEEKDAY_OPEN);
    weekday.setClosing(WEEKDAY_CLOSE);

    ScheduleProperties.DayTime saturday = new ScheduleProperties.DayTime();
    saturday.setOpening(SAT_OPEN);
    saturday.setClosing(SAT_CLOSE);

    ScheduleProperties.DayTime sunday = new ScheduleProperties.DayTime();
    sunday.setOpening(SUN_OPEN);
    sunday.setClosing(SUN_CLOSE);

    props.setWeekday(weekday);
    props.setSaturday(saturday);
    props.setSunday(sunday);

    validator = new BookingTimeValidator(props);
  }

  @Test
  void givenValidBooking_shouldReturnEmpty() {
    Optional<ValidationFailure> result = validator.validate(request(VALID_START, VALID_END));
    assertThat(result).isEmpty();
  }

  @Test
  void givenEndBeforeStart_shouldFail() {
    Optional<ValidationFailure> result = validator.validate(request(VALID_END, VALID_START));
    assertThat(result).isPresent();
    assertThat(result.get().message()).contains("before end time");
  }

  @Test
  void givenEqualStartAndEnd_shouldFail() {
    Optional<ValidationFailure> result = validator.validate(request(VALID_START, VALID_START));
    assertThat(result).isPresent();
    assertThat(result.get().message()).contains("before end time");
  }

  @Test
  void givenDurationUnder15Minutes_shouldFail() {
    LocalDateTime end = VALID_START.plusMinutes(10);
    Optional<ValidationFailure> result = validator.validate(request(VALID_START, end));
    assertThat(result).isPresent();
    assertThat(result.get().message()).contains("minimum of 15 minutes");
  }

  @Test
  void givenDurationExactly15Minutes_shouldPass() {
    LocalDateTime end = VALID_START.plusMinutes(15);
    Optional<ValidationFailure> result = validator.validate(request(VALID_START, end));
    assertThat(result).isEmpty();
  }

  @Test
  void givenStartNotOnFiveMinuteInterval_shouldFail() {
    LocalDateTime misalignedStart = LocalDateTime.of(2025, 6, 2, 10, 3);
    LocalDateTime end = misalignedStart.plusMinutes(15);
    Optional<ValidationFailure> result = validator.validate(request(misalignedStart, end));
    assertThat(result).isPresent();
    assertThat(result.get().message()).contains("start time");
  }

  @Test
  void givenStartWithSeconds_shouldFail() {
    LocalDateTime startWithSeconds = LocalDateTime.of(2025, 6, 2, 10, 0, 30);
    LocalDateTime end = startWithSeconds.plusMinutes(15);
    Optional<ValidationFailure> result = validator.validate(request(startWithSeconds, end));
    assertThat(result).isPresent();
    assertThat(result.get().message()).contains("start time");
  }

  @Test
  void givenEndNotOnFiveMinuteInterval_shouldFail() {
    LocalDateTime end = LocalDateTime.of(2025, 6, 2, 10, 17);
    Optional<ValidationFailure> result = validator.validate(request(VALID_START, end));
    assertThat(result).isPresent();
    assertThat(result.get().message()).contains("end time");
  }

  @Test
  void givenBookingSpanningMidnight_shouldFail() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 2, 21, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 3, 9, 0);
    Optional<ValidationFailure> result = validator.validate(request(start, end));
    assertThat(result).isPresent();
    assertThat(result.get().message()).contains("same day");
  }

  @Test
  void givenBookingBeforeOpeningHours_shouldFail() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 2, 7, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 2, 8, 0);
    Optional<ValidationFailure> result = validator.validate(request(start, end));
    assertThat(result).isPresent();
    assertThat(result.get().message()).contains("opening hours");
  }

  @Test
  void givenBookingAfterClosingHours_shouldFail() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 2, 22, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 2, 23, 0);
    Optional<ValidationFailure> result = validator.validate(request(start, end));
    assertThat(result).isPresent();
    assertThat(result.get().message()).contains("opening hours");
  }

  @Test
  void givenValidSaturdayBooking_shouldPass() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 7, 10, 0); // Saturday
    LocalDateTime end = LocalDateTime.of(2025, 6, 7, 11, 0);
    Optional<ValidationFailure> result = validator.validate(request(start, end));
    assertThat(result).isEmpty();
  }

  @Test
  void givenSaturdayBookingAfterClosing_shouldFail() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 7, 18, 0); // Saturday at closing
    LocalDateTime end = LocalDateTime.of(2025, 6, 7, 19, 0);
    Optional<ValidationFailure> result = validator.validate(request(start, end));
    assertThat(result).isPresent();
    assertThat(result.get().message()).contains("opening hours");
  }

  @Test
  void givenValidSundayBooking_shouldPass() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 8, 10, 0); // Sunday
    LocalDateTime end = LocalDateTime.of(2025, 6, 8, 11, 0);
    Optional<ValidationFailure> result = validator.validate(request(start, end));
    assertThat(result).isEmpty();
  }

  private BookingRequest request(LocalDateTime from, LocalDateTime to) {
    return new BookingRequest(Room.ASTAIRE, ClassType.PRIVATE, false, from, to);
  }
}

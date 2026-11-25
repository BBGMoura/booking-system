package com.acs.bookingsystem.booking.service.validation;

import com.acs.bookingsystem.booking.repository.BookingRepository;
import com.acs.bookingsystem.booking.request.BookingRequest;
import com.acs.bookingsystem.settings.service.StudioSettingsService;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BookingValidator {

  private final List<BookingValidatorRule> rules;

  public BookingValidator(
      ScheduleProperties scheduleProperties,
      BookingRepository bookingRepository,
      StudioSettingsService settingsService) {
    this.rules =
        List.of(
            new BookingTimeValidator(scheduleProperties),
            new NonShareableConflictValidator(bookingRepository),
            new ShareabilityLimitValidator(bookingRepository, settingsService));
  }

  public Optional<ValidationFailure> validate(BookingRequest request) {
    Optional<ValidationFailure> result =
        rules.stream()
            .map(rule -> rule.validate(request))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    result.ifPresent(f -> log.warn("Invalid booking request: {} for {}", f.message(), request));
    return result;
  }
}

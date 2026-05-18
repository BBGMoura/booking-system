package com.acs.bookingsystem.booking.service.validation;

import com.acs.bookingsystem.booking.repository.BookingRepository;
import com.acs.bookingsystem.booking.request.BookingRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class BookingValidator {

    private final List<BookingValidatorRule> rules;

    public BookingValidator(ScheduleProperties scheduleProperties, BookingRepository bookingRepository) {
        this.rules = List.of(
                new BookingTimeValidator(scheduleProperties),
                new BookingConflictValidator(bookingRepository),
                new BookingShareabilityValidator(bookingRepository)
        );
    }

    public Optional<ValidationFailure> validate(BookingRequest request) {
        Optional<ValidationFailure> result = rules.stream()
                                                  .map(rule -> rule.validate(request))
                                                  .filter(Optional::isPresent)
                                                  .map(Optional::get)
                                                  .findFirst();
        result.ifPresent(f -> log.warn("Invalid booking request: {} for {}", f.message(), request));
        return result;
    }
}

package com.acs.bookingsystem.booking.service.validation;

import com.acs.bookingsystem.booking.request.BookingRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class BookingValidator {

    private final List<BookingValidatorRule> validationRules;

    public Optional<String> validate(BookingRequest request) {
        for (BookingValidatorRule rule : validationRules) {
            Optional<String> validationResult = rule.validate(request);

            if (validationResult.isPresent()) {
                return validationResult;
            }
        }
        return Optional.empty();
    }

}

package org.dev.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.dev.model.BookingStatus;

import java.util.stream.Stream;

@Converter(autoApply = true)
public class BookingStatusConverter implements AttributeConverter<BookingStatus, String> {

    @Override
    public String convertToDatabaseColumn(BookingStatus bookingStatus) {
        if(bookingStatus == null) {
            return null;
        }
        return bookingStatus.getCode();
    }

    @Override
    public BookingStatus convertToEntityAttribute(String code) {
        if (code == null) {
            return null;
        }

        return Stream.of(BookingStatus.values())
                .filter(value -> value.getCode().equals(code))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}

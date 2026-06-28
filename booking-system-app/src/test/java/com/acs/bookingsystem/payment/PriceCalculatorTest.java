package com.acs.bookingsystem.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.acs.bookingsystem.danceclass.DanceClassTestData;
import com.acs.bookingsystem.danceclass.entity.DanceClass;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PriceCalculatorTest {

  private final DanceClass danceClass = DanceClassTestData.danceClassPrice;

  @ParameterizedTest(name = "Booking until {0} should cost £{1}")
  @CsvSource({
    "'2024-11-12T12:30:00', '5.0'", // 30 minutes
    "'2024-11-12T12:45:00', '7.5'", // 45 minutes
    "'2024-11-12T13:00:00', '10.0'", // 1 hour
    "'2024-11-12T14:30:00', '25.0'" // 2.5 hours
  })
  void calculateTotalPrice_forValidTimeSlots(String endDateTimeStr, String expectedPriceStr) {
    OffsetDateTime start = getStart();
    OffsetDateTime end = LocalDateTime.parse(endDateTimeStr).atOffset(ZoneOffset.UTC);
    BigDecimal expectedPrice = new BigDecimal(expectedPriceStr);

    BigDecimal actualPrice = PriceCalculator.calculateTotalPrice(start, end, danceClass);

    assertEquals(0, expectedPrice.compareTo(actualPrice));
  }

  private OffsetDateTime getStart() {
    return LocalDateTime.of(2024, Month.NOVEMBER, 12, 12, 0).atOffset(ZoneOffset.UTC);
  }
}

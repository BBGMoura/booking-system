package com.acs.bookingsystem.settings.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StudioSettingsRequestTest {

  private static Validator validator;

  @BeforeAll
  static void setUp() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  private record Fields(
      String studioName,
      int recurringMaxDates,
      int recurringMaxActive,
      int sharedRoomMax,
      String discount,
      int cancellationWindowHours,
      int paymentDueDays,
      int billingDay,
      String invoicePrefix,
      String text,
      String sortCode,
      String accountNumber) {

    static Fields valid() {
      return new Fields(
          "Studio", 5, 3, 3, "0.00", 24, 14, 1, "ACS", "text", "12-34-56", "12345678");
    }

    StudioSettingsRequest build() {
      return new StudioSettingsRequest(
          studioName,
          recurringMaxDates,
          recurringMaxActive,
          sharedRoomMax,
          new BigDecimal(discount),
          cancellationWindowHours,
          paymentDueDays,
          billingDay,
          invoicePrefix,
          text,
          text,
          text,
          sortCode,
          accountNumber);
    }
  }

  private boolean isValid(Function<Fields, Fields> change) {
    return validator.validate(change.apply(Fields.valid()).build()).isEmpty();
  }

  @Test
  void givenValidRequest_shouldPass() {
    assertThat(isValid(f -> f)).isTrue();
  }

  @ParameterizedTest(name = "recurringMaxDates {0} valid={1}")
  @CsvSource({"0,false", "1,true", "52,true", "53,false"})
  void recurringMaxDatesRange(int value, boolean valid) {
    assertThat(
            isValid(
                f ->
                    new Fields(
                        f.studioName,
                        value,
                        f.recurringMaxActive,
                        f.sharedRoomMax,
                        f.discount,
                        f.cancellationWindowHours,
                        f.paymentDueDays,
                        f.billingDay,
                        f.invoicePrefix,
                        f.text,
                        f.sortCode,
                        f.accountNumber)))
        .isEqualTo(valid);
  }

  @ParameterizedTest(name = "recurringMaxActive {0} valid={1}")
  @CsvSource({"0,false", "1,true", "20,true", "21,false"})
  void recurringMaxActiveRange(int value, boolean valid) {
    assertThat(
            isValid(
                f ->
                    new Fields(
                        f.studioName,
                        f.recurringMaxDates,
                        value,
                        f.sharedRoomMax,
                        f.discount,
                        f.cancellationWindowHours,
                        f.paymentDueDays,
                        f.billingDay,
                        f.invoicePrefix,
                        f.text,
                        f.sortCode,
                        f.accountNumber)))
        .isEqualTo(valid);
  }

  @ParameterizedTest(name = "sharedRoomMax {0} valid={1}")
  @CsvSource({"1,false", "2,true", "10,true", "11,false"})
  void sharedRoomMaxRange(int value, boolean valid) {
    assertThat(
            isValid(
                f ->
                    new Fields(
                        f.studioName,
                        f.recurringMaxDates,
                        f.recurringMaxActive,
                        value,
                        f.discount,
                        f.cancellationWindowHours,
                        f.paymentDueDays,
                        f.billingDay,
                        f.invoicePrefix,
                        f.text,
                        f.sortCode,
                        f.accountNumber)))
        .isEqualTo(valid);
  }

  @ParameterizedTest(name = "discount {0} valid={1}")
  @CsvSource({"-0.01,false", "0.00,true", "2.50,true", "1.234,false"})
  void discountRange(String value, boolean valid) {
    assertThat(
            isValid(
                f ->
                    new Fields(
                        f.studioName,
                        f.recurringMaxDates,
                        f.recurringMaxActive,
                        f.sharedRoomMax,
                        value,
                        f.cancellationWindowHours,
                        f.paymentDueDays,
                        f.billingDay,
                        f.invoicePrefix,
                        f.text,
                        f.sortCode,
                        f.accountNumber)))
        .isEqualTo(valid);
  }

  @ParameterizedTest(name = "cancellationWindowHours {0} valid={1}")
  @CsvSource({"-1,false", "0,true", "168,true", "169,false"})
  void cancellationWindowRange(int value, boolean valid) {
    assertThat(
            isValid(
                f ->
                    new Fields(
                        f.studioName,
                        f.recurringMaxDates,
                        f.recurringMaxActive,
                        f.sharedRoomMax,
                        f.discount,
                        value,
                        f.paymentDueDays,
                        f.billingDay,
                        f.invoicePrefix,
                        f.text,
                        f.sortCode,
                        f.accountNumber)))
        .isEqualTo(valid);
  }

  @ParameterizedTest(name = "paymentDueDays {0} valid={1}")
  @CsvSource({"0,false", "1,true", "90,true", "91,false"})
  void paymentDueDaysRange(int value, boolean valid) {
    assertThat(
            isValid(
                f ->
                    new Fields(
                        f.studioName,
                        f.recurringMaxDates,
                        f.recurringMaxActive,
                        f.sharedRoomMax,
                        f.discount,
                        f.cancellationWindowHours,
                        value,
                        f.billingDay,
                        f.invoicePrefix,
                        f.text,
                        f.sortCode,
                        f.accountNumber)))
        .isEqualTo(valid);
  }

  @ParameterizedTest(name = "billingDay {0} valid={1}")
  @CsvSource({"0,false", "1,true", "28,true", "29,false"})
  void billingDayRange(int value, boolean valid) {
    assertThat(
            isValid(
                f ->
                    new Fields(
                        f.studioName,
                        f.recurringMaxDates,
                        f.recurringMaxActive,
                        f.sharedRoomMax,
                        f.discount,
                        f.cancellationWindowHours,
                        f.paymentDueDays,
                        value,
                        f.invoicePrefix,
                        f.text,
                        f.sortCode,
                        f.accountNumber)))
        .isEqualTo(valid);
  }

  @ParameterizedTest(name = "invoicePrefix \"{0}\" valid={1}")
  @CsvSource({"ACS,true,", "acs,true", "AC5,false", "A-C,false", "ABCDEFGHIJK,false"})
  void invoicePrefixLettersOnly(String value, boolean valid) {
    assertThat(
            isValid(
                f ->
                    new Fields(
                        f.studioName,
                        f.recurringMaxDates,
                        f.recurringMaxActive,
                        f.sharedRoomMax,
                        f.discount,
                        f.cancellationWindowHours,
                        f.paymentDueDays,
                        f.billingDay,
                        value,
                        f.text,
                        f.sortCode,
                        f.accountNumber)))
        .isEqualTo(valid);
  }

  @Test
  void givenBlankStudioName_shouldFail() {
    assertThat(
            isValid(
                f ->
                    new Fields(
                        "   ",
                        f.recurringMaxDates,
                        f.recurringMaxActive,
                        f.sharedRoomMax,
                        f.discount,
                        f.cancellationWindowHours,
                        f.paymentDueDays,
                        f.billingDay,
                        f.invoicePrefix,
                        f.text,
                        f.sortCode,
                        f.accountNumber)))
        .isFalse();
  }

  @Test
  void givenStudioNameOver100CharactersAfterTrim_shouldFail() {
    String name = "a".repeat(101);
    assertThat(
            isValid(
                f ->
                    new Fields(
                        name,
                        f.recurringMaxDates,
                        f.recurringMaxActive,
                        f.sharedRoomMax,
                        f.discount,
                        f.cancellationWindowHours,
                        f.paymentDueDays,
                        f.billingDay,
                        f.invoicePrefix,
                        f.text,
                        f.sortCode,
                        f.accountNumber)))
        .isFalse();
  }

  @ParameterizedTest(name = "text length {0} valid={1}")
  @CsvSource({"200,true", "201,false"})
  void textFieldsAtMost200Characters(int length, boolean valid) {
    String text = "a".repeat(length);
    assertThat(
            isValid(
                f ->
                    new Fields(
                        f.studioName,
                        f.recurringMaxDates,
                        f.recurringMaxActive,
                        f.sharedRoomMax,
                        f.discount,
                        f.cancellationWindowHours,
                        f.paymentDueDays,
                        f.billingDay,
                        f.invoicePrefix,
                        text,
                        f.sortCode,
                        f.accountNumber)))
        .isEqualTo(valid);
  }

  @Test
  void givenPaddedText_shouldTrimBeforeLengthCheck() {
    String padded = "  " + "a".repeat(200) + "  ";
    StudioSettingsRequest request =
        new StudioSettingsRequest(
            "  Studio  ",
            5,
            3,
            3,
            BigDecimal.ZERO,
            24,
            14,
            1,
            " ACS ",
            padded,
            padded,
            padded,
            null,
            null);

    assertThat(validator.validate(request)).isEmpty();
    assertThat(request.studioName()).isEqualTo("Studio");
    assertThat(request.invoicePrefix()).isEqualTo("ACS");
    assertThat(request.businessName()).hasSize(200);
  }

  @Test
  void givenBlankOptionalText_shouldBecomeNull() {
    StudioSettingsRequest request =
        new StudioSettingsRequest(
            "Studio", 5, 3, 3, BigDecimal.ZERO, 24, 14, 1, "ACS", "  ", "", null, " ", "");

    assertThat(request.businessName()).isNull();
    assertThat(request.businessAddress()).isNull();
    assertThat(request.bankSortCode()).isNull();
    assertThat(request.bankAccountNumber()).isNull();
    assertThat(validator.validate(request)).isEmpty();
  }

  @ParameterizedTest(name = "sort code \"{0}\" stored as {1}")
  @CsvSource({"12-34-56,123456", "123456,123456", " 12-34-56 ,123456"})
  void sortCodeAcceptedWithOrWithoutHyphens(String input, String stored) {
    StudioSettingsRequest request =
        new StudioSettingsRequest(
            "Studio", 5, 3, 3, BigDecimal.ZERO, 24, 14, 1, "ACS", null, null, null, input, null);

    assertThat(request.bankSortCode()).isEqualTo(stored);
    assertThat(validator.validate(request)).isEmpty();
  }

  @ParameterizedTest(name = "invalid sort code \"{0}\"")
  @CsvSource({"12345", "1234567", "12-3456", "12-34-5", "ab-cd-ef", "12 34 56"})
  void invalidSortCodes(String input) {
    StudioSettingsRequest request =
        new StudioSettingsRequest(
            "Studio", 5, 3, 3, BigDecimal.ZERO, 24, 14, 1, "ACS", null, null, null, input, null);

    assertThat(validator.validate(request)).isNotEmpty();
  }

  @ParameterizedTest(name = "account number \"{0}\" valid={1}")
  @CsvSource({"12345678,true", "1234567,false", "123456789,false", "1234567a,false"})
  void accountNumberIs8Digits(String input, boolean valid) {
    StudioSettingsRequest request =
        new StudioSettingsRequest(
            "Studio", 5, 3, 3, BigDecimal.ZERO, 24, 14, 1, "ACS", null, null, null, null, input);

    assertThat(validator.validate(request).isEmpty()).isEqualTo(valid);
  }
}

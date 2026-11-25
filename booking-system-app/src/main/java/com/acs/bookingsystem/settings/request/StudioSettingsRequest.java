package com.acs.bookingsystem.settings.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record StudioSettingsRequest(
    @NotBlank @Size(max = 100) String studioName,
    @NotNull @Min(1) @Max(52) Integer recurringMaxDates,
    @NotNull @Min(1) @Max(20) Integer recurringMaxActive,
    @NotNull @Min(2) @Max(10) Integer sharedRoomMax,
    @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2)
        BigDecimal sharedDiscountPerHour,
    @NotNull @Min(0) @Max(168) Integer cancellationWindowHours,
    @NotNull @Min(1) @Max(90) Integer paymentDueDays,
    @NotNull @Min(1) @Max(28) Integer billingDay,
    @NotBlank @Size(max = 10) @Pattern(regexp = "[A-Za-z]*", message = "must contain letters only")
        String invoicePrefix,
    @Size(max = 200) String businessName,
    @Size(max = 200) String businessAddress,
    @Size(max = 200) String bankAccountName,
    @Pattern(regexp = "\\d{6}", message = "must be 6 digits, for example 12-34-56")
        String bankSortCode,
    @Pattern(regexp = "\\d{8}", message = "must be 8 digits") String bankAccountNumber) {

  private static final java.util.regex.Pattern HYPHENATED_SORT_CODE =
      java.util.regex.Pattern.compile("\\d{2}-\\d{2}-\\d{2}");

  /**
   * Text is trimmed and empty optional fields become null here, before bean validation runs, so
   * limits apply to what is stored. A sort code written as 12-34-56 is stored as 123456.
   */
  public StudioSettingsRequest {
    studioName = trimToNull(studioName);
    invoicePrefix = trimToNull(invoicePrefix);
    businessName = trimToNull(businessName);
    businessAddress = trimToNull(businessAddress);
    bankAccountName = trimToNull(bankAccountName);
    bankAccountNumber = trimToNull(bankAccountNumber);
    bankSortCode = trimToNull(bankSortCode);
    if (bankSortCode != null && HYPHENATED_SORT_CODE.matcher(bankSortCode).matches()) {
      bankSortCode = bankSortCode.replace("-", "");
    }
  }

  private static String trimToNull(String value) {
    if (value == null) return null;
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}

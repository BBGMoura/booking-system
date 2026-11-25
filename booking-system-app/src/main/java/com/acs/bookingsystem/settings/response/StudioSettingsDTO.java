package com.acs.bookingsystem.settings.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record StudioSettingsDTO(
    String studioName,
    int recurringMaxDates,
    int recurringMaxActive,
    int sharedRoomMax,
    BigDecimal sharedDiscountPerHour,
    int cancellationWindowHours,
    int paymentDueDays,
    int billingDay,
    String invoicePrefix,
    String businessName,
    String businessAddress,
    String bankAccountName,
    String bankSortCode,
    String bankAccountNumber,
    OffsetDateTime updatedAt) {}

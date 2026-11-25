package com.acs.bookingsystem.settings.controller;

import com.acs.bookingsystem.settings.entity.StudioSettings;
import com.acs.bookingsystem.settings.response.PublicStudioSettingsDTO;
import com.acs.bookingsystem.settings.response.StudioSettingsDTO;

final class StudioSettingsMapper {

  private StudioSettingsMapper() {}

  static StudioSettingsDTO toDto(StudioSettings s) {
    return new StudioSettingsDTO(
        s.getStudioName(),
        s.getRecurringMaxDates(),
        s.getRecurringMaxActive(),
        s.getSharedRoomMax(),
        s.getSharedDiscountPerHour(),
        s.getCancellationWindowHours(),
        s.getPaymentDueDays(),
        s.getBillingDay(),
        s.getInvoicePrefix(),
        s.getBusinessName(),
        s.getBusinessAddress(),
        s.getBankAccountName(),
        s.getBankSortCode(),
        s.getBankAccountNumber(),
        s.getUpdatedAt());
  }

  static PublicStudioSettingsDTO toPublicDto(StudioSettings s) {
    return new PublicStudioSettingsDTO(
        s.getStudioName(),
        s.getRecurringMaxDates(),
        s.getRecurringMaxActive(),
        s.getSharedRoomMax(),
        s.getCancellationWindowHours());
  }
}

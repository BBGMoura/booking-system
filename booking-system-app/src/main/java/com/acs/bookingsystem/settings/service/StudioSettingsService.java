package com.acs.bookingsystem.settings.service;

import com.acs.bookingsystem.common.exception.NotFoundException;
import com.acs.bookingsystem.common.exception.model.ErrorCode;
import com.acs.bookingsystem.settings.entity.StudioSettings;
import com.acs.bookingsystem.settings.repository.StudioSettingsRepository;
import com.acs.bookingsystem.settings.request.StudioSettingsRequest;
import com.acs.bookingsystem.user.entity.User;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The one place the rest of the app reads studio settings from. */
@Service
@RequiredArgsConstructor
public class StudioSettingsService {

  private final StudioSettingsRepository repository;

  @Transactional(readOnly = true)
  public StudioSettings getSettings() {
    return repository
        .findById(StudioSettings.SINGLETON_ID)
        .orElseThrow(
            () ->
                new NotFoundException(
                    "Settings have not been found. Please contact support.",
                    ErrorCode.INTERNAL_ERROR));
  }

  public int getSharedRoomMax() {
    return getSettings().getSharedRoomMax();
  }

  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @Transactional
  public StudioSettings update(StudioSettingsRequest request, User updatedBy) {
    StudioSettings settings = getSettings();
    settings.setStudioName(request.studioName());
    settings.setRecurringMaxDates(request.recurringMaxDates());
    settings.setRecurringMaxActive(request.recurringMaxActive());
    settings.setSharedRoomMax(request.sharedRoomMax());
    settings.setSharedDiscountPerHour(request.sharedDiscountPerHour());
    settings.setCancellationWindowHours(request.cancellationWindowHours());
    settings.setPaymentDueDays(request.paymentDueDays());
    settings.setBillingDay(request.billingDay());
    settings.setInvoicePrefix(request.invoicePrefix());
    settings.setBusinessName(request.businessName());
    settings.setBusinessAddress(request.businessAddress());
    settings.setBankAccountName(request.bankAccountName());
    settings.setBankSortCode(request.bankSortCode());
    settings.setBankAccountNumber(request.bankAccountNumber());
    settings.setUpdatedAt(OffsetDateTime.now());
    settings.setUpdatedBy(updatedBy);
    return repository.save(settings);
  }
}

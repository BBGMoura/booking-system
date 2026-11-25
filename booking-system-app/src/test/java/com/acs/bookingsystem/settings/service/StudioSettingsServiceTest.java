package com.acs.bookingsystem.settings.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.acs.bookingsystem.common.exception.NotFoundException;
import com.acs.bookingsystem.settings.entity.StudioSettings;
import com.acs.bookingsystem.settings.repository.StudioSettingsRepository;
import com.acs.bookingsystem.settings.request.StudioSettingsRequest;
import com.acs.bookingsystem.user.entity.User;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudioSettingsServiceTest {

  @Mock private StudioSettingsRepository repository;
  @InjectMocks private StudioSettingsService service;

  private StudioSettings existing() {
    StudioSettings settings = new StudioSettings();
    settings.setStudioName("ACS Dance Studio");
    settings.setSharedRoomMax(3);
    return settings;
  }

  @Test
  void givenSingleRow_whenGetSettings_thenReadsFixedId() {
    StudioSettings settings = existing();
    when(repository.findById(1)).thenReturn(Optional.of(settings));

    assertThat(service.getSettings()).isSameAs(settings);
    assertThat(service.getSharedRoomMax()).isEqualTo(3);
  }

  @Test
  void givenRowMissing_whenGetSettings_thenThrowsNotFound() {
    when(repository.findById(1)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getSettings()).isInstanceOf(NotFoundException.class);
  }

  @Test
  void givenRequest_whenUpdate_thenOverwritesEveryFieldAndRecordsWho() {
    when(repository.findById(1)).thenReturn(Optional.of(existing()));
    when(repository.save(any(StudioSettings.class))).thenAnswer(i -> i.getArgument(0));
    User admin = User.builder().email("admin@test.com").build();
    StudioSettingsRequest request =
        new StudioSettingsRequest(
            "New Name",
            10,
            4,
            5,
            new BigDecimal("1.50"),
            48,
            30,
            15,
            "INV",
            "Biz",
            "Addr",
            "Acct",
            "123456",
            "12345678");

    StudioSettings result = service.update(request, admin);

    assertThat(result.getId()).isEqualTo(1);
    assertThat(result.getStudioName()).isEqualTo("New Name");
    assertThat(result.getRecurringMaxDates()).isEqualTo(10);
    assertThat(result.getRecurringMaxActive()).isEqualTo(4);
    assertThat(result.getSharedRoomMax()).isEqualTo(5);
    assertThat(result.getSharedDiscountPerHour()).isEqualByComparingTo("1.50");
    assertThat(result.getCancellationWindowHours()).isEqualTo(48);
    assertThat(result.getPaymentDueDays()).isEqualTo(30);
    assertThat(result.getBillingDay()).isEqualTo(15);
    assertThat(result.getInvoicePrefix()).isEqualTo("INV");
    assertThat(result.getBusinessName()).isEqualTo("Biz");
    assertThat(result.getBusinessAddress()).isEqualTo("Addr");
    assertThat(result.getBankAccountName()).isEqualTo("Acct");
    assertThat(result.getBankSortCode()).isEqualTo("123456");
    assertThat(result.getBankAccountNumber()).isEqualTo("12345678");
    assertThat(result.getUpdatedBy()).isSameAs(admin);
    assertThat(result.getUpdatedAt()).isNotNull();
  }
}

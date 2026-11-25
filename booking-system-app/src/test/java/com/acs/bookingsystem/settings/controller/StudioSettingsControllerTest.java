package com.acs.bookingsystem.settings.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acs.bookingsystem.security.config.SecurityConfig;
import com.acs.bookingsystem.security.util.JwtUtil;
import com.acs.bookingsystem.settings.entity.StudioSettings;
import com.acs.bookingsystem.settings.request.StudioSettingsRequest;
import com.acs.bookingsystem.settings.service.StudioSettingsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({StudioSettingsAdminController.class, StudioSettingsController.class})
@Import({SecurityConfig.class})
class StudioSettingsControllerTest {

  private static final String ADMIN_URL = "/api/v1/admin/settings";
  private static final String PUBLIC_URL = "/api/v1/settings/public";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private JwtUtil jwtUtil;
  @MockitoBean private AuthenticationProvider authenticationProvider;
  @MockitoBean private StudioSettingsService settingsService;

  private StudioSettings settings() {
    StudioSettings s = new StudioSettings();
    s.setStudioName("ACS Dance Studio");
    s.setRecurringMaxDates(5);
    s.setRecurringMaxActive(3);
    s.setSharedRoomMax(3);
    s.setSharedDiscountPerHour(new BigDecimal("1.50"));
    s.setCancellationWindowHours(24);
    s.setPaymentDueDays(14);
    s.setBillingDay(1);
    s.setInvoicePrefix("ACS");
    s.setBankAccountName("ACS Ltd");
    s.setBankSortCode("123456");
    s.setBankAccountNumber("12345678");
    s.setUpdatedAt(OffsetDateTime.parse("2026-01-01T10:00:00Z"));
    return s;
  }

  private String validBody() throws Exception {
    return objectMapper.writeValueAsString(
        new StudioSettingsRequest(
            "ACS Dance Studio",
            5,
            3,
            3,
            new BigDecimal("1.50"),
            24,
            14,
            1,
            "ACS",
            null,
            null,
            "ACS Ltd",
            "12-34-56",
            "12345678"));
  }

  @Test
  @WithMockUser(roles = {"ADMIN"})
  void givenAdmin_whenGet_thenReturnsEverySetting() throws Exception {
    when(settingsService.getSettings()).thenReturn(settings());

    mockMvc
        .perform(get(ADMIN_URL))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.studioName", equalTo("ACS Dance Studio")))
        .andExpect(jsonPath("$.sharedRoomMax", equalTo(3)))
        .andExpect(jsonPath("$.sharedDiscountPerHour", equalTo(1.5)))
        .andExpect(jsonPath("$.invoicePrefix", equalTo("ACS")))
        .andExpect(jsonPath("$.bankSortCode", equalTo("123456")))
        .andExpect(jsonPath("$.bankAccountNumber", equalTo("12345678")));
  }

  @Test
  @WithMockUser(roles = {"USER"})
  void givenUser_whenGet_thenForbidden() throws Exception {
    mockMvc.perform(get(ADMIN_URL)).andExpect(status().isForbidden());
    verifyNoInteractions(settingsService);
  }

  @Test
  @WithMockUser(roles = {"ADMIN"})
  void givenAdminAndValidBody_whenPut_thenReturnsUpdatedAndNormalisesSortCode() throws Exception {
    when(settingsService.update(any(), any())).thenReturn(settings());

    mockMvc
        .perform(put(ADMIN_URL).contentType(MediaType.APPLICATION_JSON).content(validBody()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.bankSortCode", equalTo("123456")));

    verify(settingsService)
        .update(
            org.mockito.ArgumentMatchers.argThat(r -> "123456".equals(r.bankSortCode())), any());
  }

  @Test
  @WithMockUser(roles = {"ADMIN"})
  void givenAdminAndHyphenatedSortCodeJson_whenPut_thenAccepted() throws Exception {
    when(settingsService.update(any(), any())).thenReturn(settings());
    String body =
        validBody().replace("\"bankSortCode\":\"123456\"", "\"bankSortCode\":\"12-34-56\"");

    mockMvc
        .perform(put(ADMIN_URL).contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = {"ADMIN"})
  void givenAdminAndOutOfRangeNumber_whenPut_thenBadRequest() throws Exception {
    String body = validBody().replace("\"sharedRoomMax\":3", "\"sharedRoomMax\":1");

    mockMvc
        .perform(put(ADMIN_URL).contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(settingsService);
  }

  @Test
  @WithMockUser(roles = {"ADMIN"})
  void givenAdminAndBadAccountNumber_whenPut_thenBadRequest() throws Exception {
    String body = validBody().replace("12345678", "1234");

    mockMvc
        .perform(put(ADMIN_URL).contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(roles = {"ADMIN"})
  void givenAdminAndMissingField_whenPut_thenBadRequest() throws Exception {
    String body = validBody().replace("\"billingDay\":1,", "");

    mockMvc
        .perform(put(ADMIN_URL).contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(roles = {"USER"})
  void givenUser_whenPut_thenForbidden() throws Exception {
    mockMvc
        .perform(put(ADMIN_URL).contentType(MediaType.APPLICATION_JSON).content(validBody()))
        .andExpect(status().isForbidden());
    verifyNoInteractions(settingsService);
  }

  @Test
  @WithMockUser(roles = {"USER"})
  void givenUser_whenGetPublic_thenReturnsOnlyPublicFields() throws Exception {
    when(settingsService.getSettings()).thenReturn(settings());

    mockMvc
        .perform(get(PUBLIC_URL))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.*", hasSize(5)))
        .andExpect(jsonPath("$.studioName", equalTo("ACS Dance Studio")))
        .andExpect(jsonPath("$.recurringMaxDates", equalTo(5)))
        .andExpect(jsonPath("$.recurringMaxActive", equalTo(3)))
        .andExpect(jsonPath("$.sharedRoomMax", equalTo(3)))
        .andExpect(jsonPath("$.cancellationWindowHours", equalTo(24)))
        .andExpect(jsonPath("$.bankSortCode").doesNotExist())
        .andExpect(jsonPath("$.bankAccountNumber").doesNotExist());
  }

  @Test
  void givenNoAuthentication_whenGetPublic_thenRejected() throws Exception {
    mockMvc.perform(get(PUBLIC_URL)).andExpect(status().is4xxClientError());
  }
}

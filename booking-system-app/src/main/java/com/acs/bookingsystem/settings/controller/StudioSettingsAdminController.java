package com.acs.bookingsystem.settings.controller;

import com.acs.bookingsystem.security.CurrentUser;
import com.acs.bookingsystem.settings.request.StudioSettingsRequest;
import com.acs.bookingsystem.settings.response.StudioSettingsDTO;
import com.acs.bookingsystem.settings.service.StudioSettingsService;
import com.acs.bookingsystem.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/settings")
@SuppressWarnings("java:S4684") // @CurrentUser resolves from security context, not request body
public class StudioSettingsAdminController {

  private final StudioSettingsService settingsService;

  @GetMapping
  public ResponseEntity<StudioSettingsDTO> getSettings() {
    return ResponseEntity.ok(StudioSettingsMapper.toDto(settingsService.getSettings()));
  }

  @PutMapping
  public ResponseEntity<StudioSettingsDTO> updateSettings(
      @CurrentUser User user, @Valid @RequestBody StudioSettingsRequest request) {
    return ResponseEntity.ok(StudioSettingsMapper.toDto(settingsService.update(request, user)));
  }
}

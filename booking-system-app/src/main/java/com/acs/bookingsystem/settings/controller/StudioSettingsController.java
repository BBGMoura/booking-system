package com.acs.bookingsystem.settings.controller;

import com.acs.bookingsystem.settings.response.PublicStudioSettingsDTO;
import com.acs.bookingsystem.settings.service.StudioSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/settings")
public class StudioSettingsController {

  private final StudioSettingsService settingsService;

  @GetMapping("/public")
  public ResponseEntity<PublicStudioSettingsDTO> getPublicSettings() {
    return ResponseEntity.ok(StudioSettingsMapper.toPublicDto(settingsService.getSettings()));
  }
}

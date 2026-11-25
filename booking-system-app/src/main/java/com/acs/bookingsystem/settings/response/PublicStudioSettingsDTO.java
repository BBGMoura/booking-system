package com.acs.bookingsystem.settings.response;

/** The settings every signed-in user's UI needs. Never add business or bank details here. */
public record PublicStudioSettingsDTO(
    String studioName,
    int recurringMaxDates,
    int recurringMaxActive,
    int sharedRoomMax,
    int cancellationWindowHours) {}

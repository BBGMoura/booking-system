package com.acs.bookingsystem.user.request;

import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(
    @NotNull(message = "enabled cannot be null") Boolean enabled) {}

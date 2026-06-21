package com.acs.bookingsystem.user.response;

import java.util.UUID;
import lombok.Builder;

@Builder
public record UserStatusResponse(UUID uid, boolean enabled) {}

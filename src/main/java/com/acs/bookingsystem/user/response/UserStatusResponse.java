package com.acs.bookingsystem.user.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserStatusResponse(UUID uid,
                                 boolean enabled) {}

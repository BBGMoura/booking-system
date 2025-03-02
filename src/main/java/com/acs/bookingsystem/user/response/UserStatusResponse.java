package com.acs.bookingsystem.user.response;

import lombok.Builder;

@Builder
public record UserStatusResponse(int userId,
                                 boolean enabled) {
}

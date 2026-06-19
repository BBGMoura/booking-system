package com.acs.bookingsystem.user.response;

import lombok.Builder;

@Builder
public record AuthenticateResponse(String token) {
}

package com.acs.bookingsystem.authorization.response;

import lombok.Builder;

@Builder
public record AuthenticationResponse(String token) {
}

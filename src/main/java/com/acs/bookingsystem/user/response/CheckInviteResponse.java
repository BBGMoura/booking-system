package com.acs.bookingsystem.user.response;

public record CheckInviteResponse(
        String email,
        boolean invited) {
}

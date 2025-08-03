package com.acs.bookingsystem.user.response;

import lombok.Builder;

@Builder
public record UserInfoResponse(int id,
                               String firstName,
                               String lastName,
                               String phoneNumber,
                               int userId) {
}

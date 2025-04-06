package com.acs.bookingsystem.user.response;

import com.acs.bookingsystem.user.enums.Role;
import lombok.Builder;

@Builder
public record RegisterResponse(String token,
                               int userId,
                               String firstName,
                               String lastName,
                               String email,
                               String phoneNumber,
                               int userInfoId,
                               Role role,
                               boolean enabled) {
}

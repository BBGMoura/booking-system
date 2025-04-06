package com.acs.bookingsystem.user.response;

import com.acs.bookingsystem.user.enums.Role;
import lombok.Builder;

@Builder
public record InviteResponse(int userId,
                             String email,
                             Role role) {
}

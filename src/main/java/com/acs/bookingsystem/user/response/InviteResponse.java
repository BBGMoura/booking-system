package com.acs.bookingsystem.user.response;

import com.acs.bookingsystem.user.enums.Permission;
import lombok.Builder;

@Builder
public record InviteResponse(int userId,
                             String email,
                             Permission permission) {
}

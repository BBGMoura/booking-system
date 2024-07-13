package com.acs.bookingsystem.admin.response;

import com.acs.bookingsystem.authorization.enums.Permission;
import lombok.Builder;

@Builder
public record InvitationResponse(int authId,
                                 String email,
                                 Permission permission) {
}

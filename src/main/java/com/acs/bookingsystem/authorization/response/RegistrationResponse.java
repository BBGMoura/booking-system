package com.acs.bookingsystem.authorization.response;

import com.acs.bookingsystem.authorization.enums.Permission;
import lombok.Builder;

@Builder
public record RegistrationResponse(String token,
                                   int userId,
                                   String firstName,
                                   String lastName,
                                   String email,
                                   String phoneNumber,
                                   int authUserId,
                                   Permission permission,
                                   boolean enabled) {
}

package com.acs.bookingsystem.user.response;

import com.acs.bookingsystem.user.enums.Permission;
import lombok.Builder;

@Builder
public record RegistrateResponse(String token,
                                 int userId,
                                 String firstName,
                                 String lastName,
                                 String email,
                                 String phoneNumber,
                                 int userInfoId,
                                 Permission permission,
                                 boolean enabled) {
}

package com.acs.bookingsystem.user.model;

import com.acs.bookingsystem.user.enums.Permission;
import lombok.Builder;

@Builder
public record UserProfile(int userId,
                          String firstName,
                          String lastName,
                          String email,
                          String phoneNumber,
                          boolean status,
                          Permission permission) {
}

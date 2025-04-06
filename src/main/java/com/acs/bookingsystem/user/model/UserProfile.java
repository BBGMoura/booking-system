package com.acs.bookingsystem.user.model;

import com.acs.bookingsystem.user.enums.Role;
import lombok.Builder;

@Builder
public record UserProfile(int userId,
                          String firstName,
                          String lastName,
                          String email,
                          String phoneNumber,
                          boolean enabled,
                          Role role) {
}

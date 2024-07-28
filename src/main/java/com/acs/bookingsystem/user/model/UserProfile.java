package com.acs.bookingsystem.user.model;

import com.acs.bookingsystem.user.enums.Permission;

public record UserProfile(int userId,
                          String firstName,
                          String lastName,
                          String email,
                          String phoneNumber,
                          Permission permission) {
}

package com.acs.bookingsystem.user.model;

import com.acs.bookingsystem.user.enums.Role;
import lombok.Builder;

import java.util.UUID;

@Builder
public record UserProfile(UUID uid,
                          String firstName,
                          String lastName,
                          String email,
                          String phoneNumber,
                          boolean enabled,
                          Role role) {}

package com.acs.bookingsystem.user.model;

import com.acs.bookingsystem.user.enums.Role;
import java.util.UUID;
import lombok.Builder;

@Builder
public record UserProfile(
    UUID uid,
    String firstName,
    String lastName,
    String email,
    String phoneNumber,
    boolean enabled,
    Role role) {}

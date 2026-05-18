package com.acs.bookingsystem.user.response;

import com.acs.bookingsystem.user.enums.Role;
import lombok.Builder;

import java.util.UUID;

@Builder
public record RegisterResponse(String token,
                               UUID uid,
                               String firstName,
                               String lastName,
                               String email,
                               String phoneNumber,
                               Role role,
                               boolean enabled) {}

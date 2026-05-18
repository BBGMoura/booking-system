package com.acs.bookingsystem.user.response;

import com.acs.bookingsystem.user.enums.Role;
import lombok.Builder;

import java.util.UUID;

@Builder
public record InviteResponse(UUID uid,
                             String email,
                             Role role) {}

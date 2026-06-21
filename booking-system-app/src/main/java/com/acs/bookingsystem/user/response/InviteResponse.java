package com.acs.bookingsystem.user.response;

import com.acs.bookingsystem.user.enums.Role;
import java.util.UUID;
import lombok.Builder;

@Builder
public record InviteResponse(UUID uid, String email, Role role) {}

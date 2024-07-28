package com.acs.bookingsystem.user.request;

import jakarta.validation.constraints.NotBlank;

public record AuthenticationRequest(@NotBlank String email,
                                    @NotBlank String password) {
}

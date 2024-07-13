package com.acs.bookingsystem.authorization.request;

import jakarta.validation.constraints.NotBlank;

public record AuthenticationRequest(@NotBlank String email,
                                    @NotBlank String password) {
}

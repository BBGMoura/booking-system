package com.acs.bookingsystem.user.request;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(@NotBlank String email) {
}

package com.acs.bookingsystem.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(@NotBlank @Size(max = 50) String firstName,
                              @NotBlank @Size(max = 50) String lastName,
                              @Size(max = 254) @Pattern(regexp = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", message = "Invalid email format") @NotBlank String email,
                              @Pattern(regexp = "^0\\d{9,10}$", message = "Invalid phone number format (ex. 07112233445)") @NotBlank String phoneNumber,
                              @NotBlank String password) {
}

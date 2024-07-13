package com.acs.bookingsystem.authorization.request;

import com.acs.bookingsystem.authorization.enums.Permission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(@NotBlank @Size(max = 50) String firstName,
                              @NotBlank @Size(max = 50) String lastName,
                              @NotBlank @Size(max = 254) @Pattern(regexp = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", message = "Invalid email format") String email,
                              @NotBlank @Pattern(regexp = "^0\\d{8,10}$", message = "Invalid phone number format (ex. 07112233445)") String phoneNumber,
                              //TODO: add password regex
                              @NotBlank  String password) {
}

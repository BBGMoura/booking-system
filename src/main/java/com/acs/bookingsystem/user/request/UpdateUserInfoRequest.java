package com.acs.bookingsystem.user.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserInfoRequest(@Size(max = 50) String firstName,
                                    @Size(max = 50) String lastName,
                                    @Pattern(regexp = "^0\\d{9,10}$", message = "Invalid phone number format (ex. 07112233445)") String phoneNumber) {
}

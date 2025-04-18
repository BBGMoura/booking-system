package com.acs.bookingsystem.user.request;

import com.acs.bookingsystem.user.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record InviteRequest(@Size(max = 254)
                            @Pattern(regexp = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}",
                                     message = "Invalid email format")
                            @NotBlank String email,
                            @NotNull(message = "Role cannot be null")
                            Role role) {
}

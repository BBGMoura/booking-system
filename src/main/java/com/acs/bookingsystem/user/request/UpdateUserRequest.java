package com.acs.bookingsystem.user.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(@Size(max = 254)
                                @Pattern(regexp = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}",
                                                          message = "Invalid email format")
                                String email,
                                @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$£%^&+=?'~:;/.,*(){}]).{8,16}$",
                                        message = "Password must have one special char, one uppercase, one lower case and one number.")
                                String password,
                                Boolean status) {
}


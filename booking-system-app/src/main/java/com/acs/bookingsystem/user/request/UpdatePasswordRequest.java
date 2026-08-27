package com.acs.bookingsystem.user.request;

import jakarta.validation.constraints.Pattern;

public record UpdatePasswordRequest(
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$£%^&+=?'~:;/.,*(){}]).{8,16}$",
            message =
                "Password must have one special char, one uppercase, one lower case and one number.")
        String password) {}

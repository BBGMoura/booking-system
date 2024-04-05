package com.acs.bookingsystem.user.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UserUpdateRequest {
    private int id;
    @NotEmpty
    @Size(max=50)
    private String firstName;
    @NotEmpty
    @Size(max=50)
    private String lastName;
    @NotEmpty
    @Size(max=50)
    @Pattern(regexp="[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
    private String email;
    @NotEmpty
    @Pattern(regexp = "\\d{11}", message = "Invalid phone number format. It should be a 11-digits starting with 0.")
    private String phoneNumber;
}

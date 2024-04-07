package com.acs.bookingsystem.user.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserUpdateRequest {
    @NotEmpty
    @Size(max=50)
    private String firstName;
    @NotEmpty
    @Size(max=50)
    private String lastName;
    @NotEmpty
    @Size(max=254)
    @Pattern(regexp="[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", message="Invalid email format")
    private String email;
    @NotEmpty
    @Pattern(regexp = "\\d{11}", message = "Invalid phone number format (ex. 0711223344556)")
    private String phoneNumber;
}

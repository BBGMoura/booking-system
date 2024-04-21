package com.acs.bookingsystem.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserRegistrationRequest {
    @NotBlank
    @Size(max=50)
    private String firstName;
    @NotBlank
    @Size(max=50)
    private String lastName;
    @NotBlank
    @Size(max=254)
    @Pattern(regexp="[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", message="Invalid email format")
    private String email;
    @NotBlank
    @Pattern(regexp = "\\d{11}", message = "Invalid phone number format (ex. 0711223344556)")
    private String phoneNumber;
}

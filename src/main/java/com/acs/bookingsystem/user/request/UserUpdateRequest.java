package com.acs.bookingsystem.user.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserUpdateRequest {
    @Size(max=50)
    private String firstName;
    @Size(max=50)
    private String lastName;
    @Size(max=254)
    @Pattern(regexp="[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", message="Invalid email format")
    private String email;
    @Pattern(regexp = "\\d{11}", message = "Invalid phone number format (ex. 07112233445)")
    private String phoneNumber;
}

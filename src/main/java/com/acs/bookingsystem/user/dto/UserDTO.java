package com.acs.bookingsystem.user.dto;

import com.acs.bookingsystem.user.enums.Permission;

public record UserDTO(
         int id,
         String firstName,
         String lastName,
         String email,
         String phoneNumber,
         boolean active,
         Permission permission
) {
}

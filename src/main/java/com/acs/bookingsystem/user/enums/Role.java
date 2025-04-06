package com.acs.bookingsystem.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * Defines user roles within the booking system.
 * <p>
 * Ensure to:
 * <ul>
 *     <li>Include the "ROLE_" prefix when adding a new role.</li>
 *     <li>Update the role hierarchy in {@code SecurityConfig} if the new role requires a specific hierarchy.</li>
 * </ul>
 */
@AllArgsConstructor
@Getter
public enum Role {
    ROLE_ADMIN,
    ROLE_USER
}

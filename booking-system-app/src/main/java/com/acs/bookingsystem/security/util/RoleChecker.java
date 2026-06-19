package com.acs.bookingsystem.security.util;

import lombok.AllArgsConstructor;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class RoleChecker {

    private final RoleHierarchy roleHierarchy;

    /**
     * Checks if the user has the required role or any higher-level role.
     * If the user has a higher-level role, they automatically have the lower-level roles.
     *
     * @param userRole the role the user currently holds (e.g., "ROLE_ADMIN")
     * @param requiredRole the role required to perform a certain action (e.g., "ROLE_USER")
     * @return true if the user has the required role, or if their role hierarchy includes the required role.
     */
    public boolean hasRole(String userRole, String requiredRole) {
        return roleHierarchy.getReachableGrantedAuthorities(List.of(new SimpleGrantedAuthority(userRole)))
                .contains(new SimpleGrantedAuthority(requiredRole));
    }
}

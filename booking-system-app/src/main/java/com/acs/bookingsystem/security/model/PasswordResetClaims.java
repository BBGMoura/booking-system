package com.acs.bookingsystem.security.model;

public record PasswordResetClaims(String email, String passwordHash) {}

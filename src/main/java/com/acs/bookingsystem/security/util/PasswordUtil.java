package com.acs.bookingsystem.security.util;

import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Random;

@Component
@AllArgsConstructor
public class PasswordUtil {

    private static final String ALLOWED_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@'./:;&";
    private static final int DEFAULT_LENGTH = 12;
    private final Random random = new SecureRandom();
    private final PasswordEncoder passwordEncoder;

    public String generateNewPassword() {
        return generatePassword(DEFAULT_LENGTH);
    }

    public String generatePassword(int length) {
        StringBuilder stringBuilder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(ALLOWED_CHARACTERS.length());
            stringBuilder.append(ALLOWED_CHARACTERS.charAt(index));
        }

        return stringBuilder.toString();
    }

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}

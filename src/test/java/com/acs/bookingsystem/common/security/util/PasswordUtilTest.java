package com.acs.bookingsystem.common.security.util;

import com.acs.bookingsystem.security.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class PasswordUtilTest {

    private PasswordUtil passwordUtil;

    @BeforeEach
    void setUp() {
        passwordUtil = new PasswordUtil(new BCryptPasswordEncoder());
    }

    @Test
    void encodePassword() {
        String rawPassword = "Aa123456!";

        String encodedPassword = passwordUtil.encodePassword(rawPassword);
        System.out.println(encodedPassword);
    }
}
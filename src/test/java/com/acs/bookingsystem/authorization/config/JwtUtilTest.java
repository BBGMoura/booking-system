package com.acs.bookingsystem.authorization.config;

import com.acs.bookingsystem.security.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Field;


import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private static final String SECRET_KEY = "Z3C79B4RkvUor/BPyjQOUHHRyxeg5EIT1TtCFK0yNCA=";

    @Mock
    private UserDetails userDetails;

    private JwtUtil jwtUtil;

    @BeforeEach
    public void setUp() throws Exception {
        jwtUtil = new JwtUtil();
        Field field = jwtUtil.getClass().getDeclaredField("secretKey");
        field.setAccessible(true);
        field.set(jwtUtil, JwtUtilTest.SECRET_KEY);
        userDetails = User.withUsername("tester").password("password").authorities("ROLE_USER").build();
    }

    @Test
    void testGenerateToken() {
        String token = jwtUtil.generateToken(userDetails);
        assertFalse(token.isEmpty());
    }

    @Test
    void testExtractUsername() {
        String token = jwtUtil.generateToken(userDetails);
        String username = jwtUtil.extractUsername(token);
        assertEquals("tester", username);
    }

    @Test
    void testIsTokenValid() {
        String token = jwtUtil.generateToken(userDetails);
        assertTrue(jwtUtil.isTokenValid(token, userDetails));
    }
}

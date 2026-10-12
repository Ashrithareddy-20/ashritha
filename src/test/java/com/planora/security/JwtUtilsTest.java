package com.planora.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;
    // 256-bit key in plain ASCII (64 characters)
    private final String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long expirationMs = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils(secret, expirationMs);
    }

    @Test
    void generateToken_AndExtractClaims_ShouldSucceed() {
        String token = jwtUtils.generateToken("user@planora.com", "USER", 42L);

        assertNotNull(token);
        assertTrue(jwtUtils.validateToken(token));
        assertEquals("user@planora.com", jwtUtils.extractEmail(token));
        assertEquals("USER", jwtUtils.extractRole(token));
        assertEquals(42L, jwtUtils.extractUserId(token));
    }

    @Test
    void validateToken_WhenTokenInvalid_ShouldReturnFalse() {
        assertFalse(jwtUtils.validateToken("invalid.token.here"));
    }

    @Test
    void validateToken_WhenExpired_ShouldReturnFalse() {
        // Expired 1000ms ago
        JwtUtils expiredJwtUtils = new JwtUtils(secret, -1000L);
        String token = expiredJwtUtils.generateToken("user@planora.com", "USER", 42L);

        assertFalse(expiredJwtUtils.validateToken(token));
    }
}

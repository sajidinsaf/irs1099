package com.irs1099.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        // 256-bit key (32+ chars required for HS384)
        tokenProvider = new JwtTokenProvider(
                "test-secret-key-that-is-at-least-32-characters-long-for-hmac",
                3600000,  // 1 hour
                604800000 // 7 days
        );
    }

    @Test
    void generateAccessToken_returnsValidToken() {
        String token = tokenProvider.generateAccessToken("test@example.com");

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(tokenProvider.validateToken(token));
    }

    @Test
    void generateRefreshToken_returnsValidToken() {
        String token = tokenProvider.generateRefreshToken("test@example.com");

        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
    }

    @Test
    void getEmailFromToken_returnsCorrectEmail() {
        String email = "test@example.com";
        String token = tokenProvider.generateAccessToken(email);

        assertEquals(email, tokenProvider.getEmailFromToken(token));
    }

    @Test
    void validateToken_withValidToken_returnsTrue() {
        String token = tokenProvider.generateAccessToken("test@example.com");
        assertTrue(tokenProvider.validateToken(token));
    }

    @Test
    void validateToken_withInvalidToken_returnsFalse() {
        assertFalse(tokenProvider.validateToken("invalid.token.here"));
    }

    @Test
    void validateToken_withNullToken_returnsFalse() {
        assertFalse(tokenProvider.validateToken(null));
    }

    @Test
    void validateToken_withEmptyToken_returnsFalse() {
        assertFalse(tokenProvider.validateToken(""));
    }

    @Test
    void validateToken_withExpiredToken_returnsFalse() {
        // Create provider with 0ms expiration
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(
                "test-secret-key-that-is-at-least-32-characters-long-for-hmac",
                0, 0
        );
        String token = shortLivedProvider.generateAccessToken("test@example.com");

        assertFalse(shortLivedProvider.validateToken(token));
    }

    @Test
    void differentTokensForDifferentEmails() {
        String token1 = tokenProvider.generateAccessToken("user1@example.com");
        String token2 = tokenProvider.generateAccessToken("user2@example.com");

        assertNotEquals(token1, token2);
        assertEquals("user1@example.com", tokenProvider.getEmailFromToken(token1));
        assertEquals("user2@example.com", tokenProvider.getEmailFromToken(token2));
    }
}

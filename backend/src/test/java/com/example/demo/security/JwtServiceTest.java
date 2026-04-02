package com.example.demo.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Test
    void generateAccessToken_shouldReturnValidToken() {
        String token = jwtService.generateAccessToken(1L, "test@example.com");
        assertNotNull(token);
        assertTrue(jwtService.isValid(token));
        assertEquals("test@example.com", jwtService.getEmail(token));
        assertEquals(1L, jwtService.getUserId(token));
    }

    @Test
    void generateRefreshToken_shouldReturnValidToken() {
        String token = jwtService.generateRefreshToken(1L, "test@example.com");
        assertNotNull(token);
        assertTrue(jwtService.isValid(token));
        assertEquals("test@example.com", jwtService.getEmail(token));
    }

    @Test
    void parseToken_withValidToken_shouldReturnClaims() {
        String token = jwtService.generateAccessToken(1L, "test@example.com");
        Claims claims = jwtService.parseToken(token);
        assertNotNull(claims);
        assertEquals("test@example.com", claims.getSubject());
        assertEquals(1, ((Number)claims.get("userId")).intValue());
    }

    @Test
    void parseToken_withInvalidToken_shouldThrowException() {
        assertThrows(Exception.class, () -> jwtService.parseToken("invalid-token"));
    }

    @Test
    void isValid_withValidToken_shouldReturnTrue() {
        String token = jwtService.generateAccessToken(1L, "test@example.com");
        assertTrue(jwtService.isValid(token));
    }

    @Test
    void isValid_withInvalidToken_shouldReturnFalse() {
        assertFalse(jwtService.isValid("invalid-token"));
    }

    @Test
    void getEmail_shouldReturnCorrectEmail() {
        String token = jwtService.generateAccessToken(1L, "test@example.com");
        assertEquals("test@example.com", jwtService.getEmail(token));
    }

    @Test
    void getUserId_shouldReturnCorrectId() {
        String token = jwtService.generateAccessToken(123L, "test@example.com");
        assertEquals(123L, jwtService.getUserId(token));
    }
}

package com.example.demo.security;

import com.example.demo.security.JwtService;
import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.security.JwtAuthFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    private JwtAuthFilter filter;
    private JwtService jwtService;
    private CustomUserDetailsService userDetailsService;
    private HttpServletRequest req;
    private HttpServletResponse res;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        userDetailsService = mock(CustomUserDetailsService.class);
        filter = new JwtAuthFilter(jwtService, userDetailsService);
        req = mock(HttpServletRequest.class);
        res = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_withValidToken_setsAuth() throws ServletException, IOException {
        String token = "valid-token";
        String email = "test@ex.com";
        UserDetails details = mock(UserDetails.class);

        when(req.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isValid(token)).thenReturn(true);
        when(jwtService.getEmail(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(details);

        filter.doFilter(req, res, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(req, res);
    }

    @Test
    void doFilterInternal_withInvalidToken_doesNotSetAuth() throws ServletException, IOException {
        String token = "invalid-token";

        when(req.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isValid(token)).thenReturn(false);

        filter.doFilter(req, res, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(req, res);
    }

    @Test
    void doFilterInternal_withoutHeader_doesNotSetAuth() throws ServletException, IOException {
        when(req.getHeader("Authorization")).thenReturn(null);

        filter.doFilter(req, res, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(req, res);
    }

    @Test
    void doFilterInternal_withExpiredToken_doesNotSetAuth() throws ServletException, IOException {
        String token = "expired-token";
        when(req.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isValid(token)).thenReturn(false);

        filter.doFilter(req, res, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(req, res);
    }

    @Test
    void doFilterInternal_withInvalidHeader_doesNotSetAuth() throws ServletException, IOException {
        when(req.getHeader("Authorization")).thenReturn("Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==");

        filter.doFilter(req, res, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(req, res);
    }
}

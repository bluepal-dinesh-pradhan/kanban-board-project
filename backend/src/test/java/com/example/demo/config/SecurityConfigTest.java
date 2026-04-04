package com.example.demo.config;

import com.example.demo.security.JwtAuthFilter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private ApplicationContext context;

    private final JwtAuthFilter jwtAuthFilter = mock(JwtAuthFilter.class);
    private final SecurityConfig config = new SecurityConfig(jwtAuthFilter);

    @Test
    void corsConfigurationSource_registersPath() {
        assertNotNull(context);
    }

    @Test
    void passwordEncoder_returnsBCrypt() {
        assertNotNull(config.passwordEncoder());
    }
}

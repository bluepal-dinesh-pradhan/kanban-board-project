package com.example.demo.security;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    private CustomUserDetailsService service;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        service = new CustomUserDetailsService(userRepository);
    }

    @Test
    void loadUserByUsername_withExistingUser_returnsUserDetails() {
        String email = "test@example.com";
        User user = User.builder()
                .id(1L)
                .email(email)
                .password("password")
                .fullName("Test User")
                .build();
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername(email);

        assertNotNull(details);
        assertEquals(email, details.getUsername());
    }

    @Test
    void loadUserByUsername_withNonExistingUser_throwsUsernameNotFoundException() {
        String email = "nonexistent@example.com";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername(email));
    }
}

package com.example.demo.controller;

import com.example.demo.dto.ChangePasswordRequest;
import com.example.demo.dto.NotificationPreferencesRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private UserPrincipal userPrincipal;
    private User testUser;

    @BeforeEach
    void setUp() {
        userPrincipal = new UserPrincipal(1L, "user@example.com", "pass", "User Name");
        testUser = User.builder().id(1L).email("user@example.com").password("encoded").fullName("User Name").build();
    }

    @Test
    void getProfile_returns200() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/users/profile")
                        .with(user(userPrincipal)))
                .andExpect(status().isOk());
    }

    @Test
    void updateProfile_returns200() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(testUser);

        mockMvc.perform(patch("/api/users/profile")
                        .with(user(userPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"New Name\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void updateNotificationPreferences_returns200() throws Exception {
        NotificationPreferencesRequest notifReq = NotificationPreferencesRequest.builder().emailNotifications(true).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        mockMvc.perform(patch("/api/users/notification-preferences")
                        .with(user(userPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notifReq)))
                .andExpect(status().isOk());
    }

    @Test
    void changePassword_withValidData_returns200() throws Exception {
        ChangePasswordRequest cpReq = new ChangePasswordRequest("oldPass", "NewPass123!");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPass", "encoded")).thenReturn(true);
        when(passwordEncoder.matches("NewPass123!", "encoded")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("newEncoded");

        mockMvc.perform(post("/api/users/change-password")
                        .with(user(userPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cpReq)))
                .andExpect(status().isOk());
    }
}

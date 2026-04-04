package com.example.demo.controller;

import com.example.demo.security.UserPrincipal;
import com.example.demo.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        userPrincipal = new UserPrincipal(1L, "user@example.com", "pass", "User Name");
    }

    @Test
    void getNotifications_returns200() throws Exception {
        when(notificationService.getUserNotifications(anyLong())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/notifications")
                        .with(user(userPrincipal)))
                .andExpect(status().isOk());
    }

    @Test
    void getUnreadCount_returns200() throws Exception {
        when(notificationService.getUnreadCount(anyLong())).thenReturn(5L);

        mockMvc.perform(get("/api/notifications/unread-count")
                        .with(user(userPrincipal)))
                .andExpect(status().isOk());
    }

    @Test
    void markAsRead_returns200() throws Exception {
        mockMvc.perform(patch("/api/notifications/1/read")
                        .with(user(userPrincipal)))
                .andExpect(status().isOk());
    }

    @Test
    void markAllRead_returns200() throws Exception {
        mockMvc.perform(patch("/api/notifications/mark-all-read")
                        .with(user(userPrincipal)))
                .andExpect(status().isOk());
    }
}

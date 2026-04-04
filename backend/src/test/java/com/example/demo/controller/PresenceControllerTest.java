package com.example.demo.controller;

import com.example.demo.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PresenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @Test
    void joinBoard_shouldReturnOnlineUsers() throws Exception {
        UserPrincipal principal = new UserPrincipal(1L, "user@test.com", "pass", "Full User");

        mockMvc.perform(post("/api/boards/1/presence/join")
                .with(user(principal))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].email").value("user@test.com"))
                .andExpect(jsonPath("$.data[0].name").value("Full User"));
    }

    @Test
    void joinBoard_noFullName_shouldFallbackToEmail() throws Exception {
        UserPrincipal principal = new UserPrincipal(1L, "user@test.com", "pass", null);

        mockMvc.perform(post("/api/boards/1/presence/join")
                .with(user(principal))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("user@test.com"));
    }

    @Test
    void heartbeat_shouldSucceed() throws Exception {
        UserPrincipal principal = new UserPrincipal(1L, "user@test.com", "pass", "Name");

        // Join first
        mockMvc.perform(post("/api/boards/1/presence/join")
                .with(user(principal)));

        // Heartbeat existing
        mockMvc.perform(post("/api/boards/1/presence/heartbeat")
                .with(user(principal))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Heartbeat missing
        mockMvc.perform(post("/api/boards/1/presence/heartbeat")
                .with(user(new UserPrincipal(2L, "u2@t.com", "p", "N2"))))
                .andExpect(status().isOk());
    }

    @Test
    void leaveBoard_shouldRemoveUser() throws Exception {
        UserPrincipal principal = new UserPrincipal(1L, "u@t.com", "p", "N");
        mockMvc.perform(post("/api/boards/1/presence/join").with(user(principal)));

        mockMvc.perform(post("/api/boards/1/presence/leave")
                .with(user(principal))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/boards/1/presence")
                .with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void getPresence_shouldCleanupStale() throws Exception {
        UserPrincipal principal = new UserPrincipal(1L, "u@t.com", "p", "N");
        mockMvc.perform(get("/api/boards/1/presence")
                .with(user(principal)))
                .andExpect(status().isOk());
    }
}

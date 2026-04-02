package com.example.demo.controller;

import com.example.demo.dto.ColumnDto;
import com.example.demo.dto.ColumnRequest;
import com.example.demo.dto.ColumnUpdateRequest;
import com.example.demo.dto.MoveColumnRequest;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.ColumnService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ColumnControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ColumnService columnService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        userPrincipal = new UserPrincipal(1L, "user@example.com", "pass", "User Name");
    }

    @Test
    void createColumn_returns201() throws Exception {
        ColumnRequest req = new ColumnRequest();
        req.setTitle("Column");

        when(columnService.create(anyLong(), any(), anyLong())).thenReturn(new ColumnDto());

        mockMvc.perform(post("/api/boards/1/columns")
                        .with(user(userPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void createColumn_withEmptyName_returns400() throws Exception {
        ColumnRequest req = new ColumnRequest();
        req.setTitle("");

        mockMvc.perform(post("/api/boards/1/columns")
                        .with(user(userPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateColumn_returns200() throws Exception {
        ColumnUpdateRequest req = new ColumnUpdateRequest();
        req.setTitle("New Title");

        when(columnService.update(anyLong(), any(), anyLong())).thenReturn(new ColumnDto());

        mockMvc.perform(patch("/api/columns/1")
                        .with(user(userPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void updateColumn_nonExisting_returns404() throws Exception {
        when(columnService.update(eq(999L), any(), anyLong()))
                .thenThrow(new com.example.demo.exception.ResourceNotFoundException("Column not found"));

        ColumnUpdateRequest req = new ColumnUpdateRequest();
        req.setTitle("Title");

        mockMvc.perform(patch("/api/columns/999")
                        .with(user(userPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteColumn_returns200() throws Exception {
        mockMvc.perform(delete("/api/columns/1")
                        .with(user(userPrincipal)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteColumn_nonExisting_returns404() throws Exception {
        doThrow(new com.example.demo.exception.ResourceNotFoundException("Column not found"))
                .when(columnService).delete(eq(999L), anyLong());

        mockMvc.perform(delete("/api/columns/999")
                        .with(user(userPrincipal)))
                .andExpect(status().isNotFound());
    }

    @Test
    void moveColumn_returns200() throws Exception {
        MoveColumnRequest req = new MoveColumnRequest();
        req.setNewPosition(2);

        mockMvc.perform(post("/api/columns/1/move")
                        .with(user(userPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void operationsWithoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/boards/1/columns"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(patch("/api/columns/1"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(delete("/api/columns/1"))
                .andExpect(status().isUnauthorized());
    }
}

package com.example.demo.controller;

import com.example.demo.dto.BoardDto;
import com.example.demo.dto.BoardRequest;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.ActivityService;
import com.example.demo.service.BoardService;
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

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BoardService boardService;

    @MockBean
    private ActivityService activityService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        userPrincipal = new UserPrincipal(1L, "user@example.com", "pass", "User Name");
    }

    @Test
    void getBoards_returns200WithAuth() throws Exception {
        when(boardService.getUserBoards(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/boards")
                        .with(user(userPrincipal)))
                .andExpect(status().isOk());
    }

    @Test
    void getBoards_returns401WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/boards"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createBoard_createsBoardWithAuth() throws Exception {
        BoardRequest req = new BoardRequest();
        req.setTitle("New Board");

        BoardDto dto = new BoardDto();
        dto.setId(1L);
        dto.setTitle("New Board");

        when(boardService.create(any(), anyLong())).thenReturn(dto);

        mockMvc.perform(post("/api/boards")
                        .with(user(userPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void deleteBoard_asOwner_returns200() throws Exception {
        mockMvc.perform(delete("/api/boards/1")
                        .with(user(userPrincipal)))
                .andExpect(status().isOk());
    }

    @Test
    void getColumns_returns200() throws Exception {
        when(boardService.getBoardColumns(anyLong(), anyLong())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/boards/1/columns")
                        .with(user(userPrincipal)))
                .andExpect(status().isOk());
    }

    @Test
    void toggleStar_returns200() throws Exception {
        when(boardService.toggleStar(anyLong(), anyLong())).thenReturn(true);

        mockMvc.perform(post("/api/boards/1/star")
                        .with(user(userPrincipal)))
                .andExpect(status().isOk());
    }

    @Test
    void getAnalytics_returns200() throws Exception {
        // This endpoint is in AnalyticsController but shares the same path prefix
        mockMvc.perform(get("/api/boards/1/analytics")
                        .with(user(userPrincipal)))
                .andExpect(status().isOk());
    }
}

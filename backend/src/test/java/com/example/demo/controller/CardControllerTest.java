package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.CardDto;
import com.example.demo.dto.CardRequest;
import com.example.demo.dto.MoveCardRequest;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.CardService;
import com.example.demo.service.CommentService;
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
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @MockBean
    private CommentService commentService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        userPrincipal = new UserPrincipal(1L, "user@example.com", "pass", "User Name");
    }

    @Test
    void getCard_returns200() throws Exception {
        when(cardService.getCard(anyLong(), anyLong())).thenReturn(new CardDto());

        mockMvc.perform(get("/api/cards/1")
                        .with(user(userPrincipal)))
                .andExpect(status().isOk());
    }

    @Test
    void createCard_returns201() throws Exception {
        CardRequest req = new CardRequest();
        req.setTitle("New Card");
        req.setColumnId(1L);

        when(cardService.create(anyLong(), any(), anyLong())).thenReturn(new CardDto());

        mockMvc.perform(post("/api/cards/boards/1")
                        .with(user(userPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void moveCard_returns200() throws Exception {
        MoveCardRequest req = new MoveCardRequest();
        req.setTargetColumnId(2L);
        req.setNewPosition(0);

        mockMvc.perform(post("/api/cards/1/move")
                        .with(user(userPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void archiveCard_returns200() throws Exception {
        mockMvc.perform(post("/api/cards/1/archive")
                        .with(user(userPrincipal)))
                .andExpect(status().isOk());
    }

    @Test
    void getComments_returns200() throws Exception {
        when(commentService.getComments(anyLong())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/cards/1/comments")
                        .with(user(userPrincipal)))
                .andExpect(status().isOk());
    }
}

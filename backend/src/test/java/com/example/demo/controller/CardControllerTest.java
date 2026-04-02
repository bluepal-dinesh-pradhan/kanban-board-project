package com.example.demo.controller;

import com.example.demo.dto.*;
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
import java.util.Map;

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
    void getCard_withNonExistingId_returns404() throws Exception {
        when(cardService.getCard(eq(999L), anyLong()))
                .thenThrow(new com.example.demo.exception.ResourceNotFoundException("Card not found"));

        mockMvc.perform(get("/api/cards/999")
                        .with(user(userPrincipal)))
                .andExpect(status().isNotFound());
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
    void createCard_withEmptyTitle_returns400() throws Exception {
        CardRequest req = new CardRequest();
        req.setTitle("");
        req.setColumnId(1L);

        mockMvc.perform(post("/api/cards/boards/1")
                        .with(user(userPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCard_withInvalidData_returns400() throws Exception {
        CardRequest req = new CardRequest();
        req.setTitle(""); // empty title

        mockMvc.perform(patch("/api/cards/1")
                        .with(user(userPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
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
    void moveCard_toNonExistingColumn_returns404() throws Exception {
        MoveCardRequest req = new MoveCardRequest();
        req.setTargetColumnId(999L);
        req.setNewPosition(0);

        when(cardService.move(anyLong(), any(), anyLong()))
                .thenThrow(new com.example.demo.exception.ResourceNotFoundException("Column not found"));

        mockMvc.perform(post("/api/cards/1/move")
                        .with(user(userPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void archiveCard_returns200() throws Exception {
        mockMvc.perform(post("/api/cards/1/archive")
                        .with(user(userPrincipal)))
                .andExpect(status().isOk());
    }

    @Test
    void archiveCard_alreadyArchived_returns400() throws Exception {
        when(cardService.archive(anyLong(), anyLong()))
                .thenThrow(new com.example.demo.exception.BadRequestException("Card is already archived"));

        mockMvc.perform(post("/api/cards/1/archive")
                        .with(user(userPrincipal)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addComment_withEmptyContent_returns400() throws Exception {
        com.example.demo.dto.CommentRequest req = new com.example.demo.dto.CommentRequest();
        req.setContent("");

        mockMvc.perform(post("/api/cards/1/comments")
                        .with(user(userPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteComment_nonExisting_returns404() throws Exception {
        doThrow(new com.example.demo.exception.ResourceNotFoundException("Comment not found"))
                .when(commentService).deleteComment(anyLong(), anyLong(), anyLong(), anyLong());

        mockMvc.perform(delete("/api/cards/boards/1/cards/1/comments/999")
                        .with(user(userPrincipal)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getComments_returns200() throws Exception {
        when(commentService.getComments(anyLong())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/cards/1/comments")
                        .with(user(userPrincipal)))
                .andExpect(status().isOk());
    }

    @Test
    void updateCard_returns200() throws Exception {
        CardRequest req = new CardRequest();
        req.setTitle("Updated Card Title");

        when(cardService.update(anyLong(), any(), anyLong())).thenReturn(new CardDto());

        mockMvc.perform(patch("/api/cards/1")
                        .with(user(userPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void updatePriority_returns200() throws Exception {
        Map<String, String> body = Map.of("priority", "HIGH");
        when(cardService.updatePriority(anyLong(), anyString(), anyLong())).thenReturn(new CardDto());

        mockMvc.perform(patch("/api/cards/1/priority")
                        .with(user(userPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    void updatePriority_withoutPriority_returns400() throws Exception {
        Map<String, String> body = Map.of();

        mockMvc.perform(patch("/api/cards/1/priority")
                        .with(user(userPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void assignCard_returns200() throws Exception {
        Map<String, Long> body = Map.of("assigneeId", 2L);
        when(cardService.assignCard(anyLong(), anyLong(), anyLong())).thenReturn(new CardDto());

        mockMvc.perform(patch("/api/cards/1/assign")
                        .with(user(userPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    void duplicateCard_returns201() throws Exception {
        when(cardService.duplicate(anyLong(), anyLong())).thenReturn(new CardDto());

        mockMvc.perform(post("/api/cards/1/duplicate")
                        .with(user(userPrincipal)))
                .andExpect(status().isCreated());
    }

    @Test
    void restoreCard_returns200() throws Exception {
        when(cardService.restore(anyLong(), anyLong())).thenReturn(new CardDto());

        mockMvc.perform(post("/api/cards/1/restore")
                        .with(user(userPrincipal)))
                .andExpect(status().isOk());
    }

    @Test
    void getArchivedCards_returns200() throws Exception {
        when(cardService.getArchivedCards(anyLong(), anyLong())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/cards/boards/1/archived")
                        .with(user(userPrincipal)))
                .andExpect(status().isOk());
    }

    @Test
    void getComments_withPagination_returns200() throws Exception {
        when(cardService.getComments(anyLong(), anyInt(), anyInt())).thenReturn(new com.example.demo.dto.PageResponse<CommentDto>(Collections.emptyList(), 0, 10, 0, 0, false, false));

        mockMvc.perform(get("/api/cards/1/comments")
                        .param("page", "0")
                        .param("size", "10")
                        .with(user(userPrincipal)))
                .andExpect(status().isOk());
    }

    @Test
    void addComment_returns200() throws Exception {
        CommentRequest req = new CommentRequest();
        req.setContent("Test comment");
        when(commentService.addComment(anyLong(), any(), anyLong())).thenReturn(new CommentDto());

        mockMvc.perform(post("/api/cards/1/comments")
                        .with(user(userPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteComment_returns200() throws Exception {
        mockMvc.perform(delete("/api/cards/boards/1/cards/1/comments/1")
                        .with(user(userPrincipal)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteCard_returns200() throws Exception {
        mockMvc.perform(delete("/api/cards/1")
                        .with(user(userPrincipal)))
                .andExpect(status().isOk());
    }

    @Test
    void operationsWithoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/cards/1"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(post("/api/cards/boards/1"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(delete("/api/cards/1"))
                .andExpect(status().isUnauthorized());
    }
}

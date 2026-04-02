package com.example.demo.controller;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ChecklistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private BoardMemberRepository boardMemberRepository;

    @Autowired
    private BoardColumnRepository boardColumnRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private ChecklistRepository checklistRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;
    private User user;
    private Card card;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .email("checklist@example.com")
                .password("password")
                .fullName("Checklist User")
                .build());

        Board board = boardRepository.save(Board.builder()
                .title("Checklist Board")
                .owner(user)
                .build());

        boardMemberRepository.save(BoardMember.builder()
                .board(board)
                .user(user)
                .role(BoardMember.Role.OWNER)
                .build());

        BoardColumn column = boardColumnRepository.save(BoardColumn.builder()
                .title("Column")
                .board(board)
                .position(0)
                .build());

        card = cardRepository.save(Card.builder()
                .title("Card")
                .column(column)
                .position(0)
                .build());

        token = "Bearer " + jwtService.generateAccessToken(user.getId(), user.getEmail());
    }

    @Test
    void getItems_returns200() throws Exception {
        checklistRepository.save(Checklist.builder()
                .title("Item 1")
                .card(card)
                .position(0)
                .build());

        mockMvc.perform(get("/api/cards/" + card.getId() + "/checklists")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void addItem_withValidData_returns201() throws Exception {
        Map<String, String> body = Map.of("title", "New Item");

        mockMvc.perform(post("/api/cards/" + card.getId() + "/checklists")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("New Item"));
    }

    @Test
    void addItem_withEmptyTitle_returns400() throws Exception {
        Map<String, String> body = Map.of("title", "");

        mockMvc.perform(post("/api/cards/" + card.getId() + "/checklists")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void toggleItem_returns200() throws Exception {
        Checklist item = checklistRepository.save(Checklist.builder()
                .title("Toggle Item")
                .card(card)
                .completed(false)
                .build());

        mockMvc.perform(patch("/api/checklists/" + item.getId() + "/toggle")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.completed").value(true));
    }

    @Test
    void updateItem_returns200() throws Exception {
        Checklist item = checklistRepository.save(Checklist.builder()
                .title("Old Title")
                .card(card)
                .build());

        Map<String, String> body = Map.of("title", "Updated Title");

        mockMvc.perform(patch("/api/checklists/" + item.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated Title"));
    }

    @Test
    void updateItem_withEmptyTitle_returns400() throws Exception {
        Checklist item = checklistRepository.save(Checklist.builder()
                .title("Old Title")
                .card(card)
                .build());

        Map<String, String> body = Map.of("title", "");

        mockMvc.perform(patch("/api/checklists/" + item.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteItem_returns200() throws Exception {
        Checklist item = checklistRepository.save(Checklist.builder()
                .title("Delete Me")
                .card(card)
                .build());

        mockMvc.perform(delete("/api/checklists/" + item.getId())
                        .header("Authorization", token))
                .andExpect(status().isOk());
    }

    @Test
    void withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/cards/" + card.getId() + "/checklists"))
                .andExpect(status().isUnauthorized());
    }
}

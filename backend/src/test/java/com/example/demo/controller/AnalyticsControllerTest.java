package com.example.demo.controller;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class AnalyticsControllerTest {

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
    private ActivityRepository activityRepository;

    @Autowired
    private JwtService jwtService;

    private String token;
    private User user;
    private Board board;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .email("analytics@example.com")
                .password("password")
                .fullName("Analytics User")
                .build());

        board = boardRepository.save(Board.builder()
                .title("Analytics Board")
                .owner(user)
                .build());

        boardMemberRepository.save(BoardMember.builder()
                .board(board)
                .user(user)
                .role(BoardMember.Role.OWNER)
                .build());

        BoardColumn colTodo = boardColumnRepository.save(BoardColumn.builder()
                .title("To Do")
                .board(board)
                .position(0)
                .build());

        BoardColumn colDone = boardColumnRepository.save(BoardColumn.builder()
                .title("Done")
                .board(board)
                .position(1)
                .build());

        // Card in To Do (URGENT, overdue)
        cardRepository.save(Card.builder()
                .title("Urgent Task")
                .column(colTodo)
                .position(0)
                .priority(Priority.URGENT)
                .dueDate(LocalDate.now().minusDays(1))
                .build());

        // Card in To Do (HIGH, not overdue)
        cardRepository.save(Card.builder()
                .title("High Task")
                .column(colTodo)
                .position(1)
                .priority(Priority.HIGH)
                .dueDate(LocalDate.now().plusDays(1))
                .build());

        // Card in Done
        cardRepository.save(Card.builder()
                .title("Done Task")
                .column(colDone)
                .position(0)
                .priority(Priority.MEDIUM)
                .build());

        // Log some activity
        activityRepository.save(Activity.builder()
                .board(board)
                .user(user)
                .action("CREATED_CARD")
                .entityType("CARD")
                .entityId(1L)
                .createdAt(LocalDateTime.now())
                .build());

        token = "Bearer " + jwtService.generateAccessToken(user.getId(), user.getEmail());
    }

    @Test
    void getAnalytics_returns200() throws Exception {
        mockMvc.perform(get("/api/boards/" + board.getId() + "/analytics")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCards").value(3))
                .andExpect(jsonPath("$.data.overdueCards").value(1))
                .andExpect(jsonPath("$.data.completedCards").value(1))
                .andExpect(jsonPath("$.data.completionRate").value(33.3))
                .andExpect(jsonPath("$.data.cardsByPriority[0].priority").value("URGENT"))
                .andExpect(jsonPath("$.data.cardsByPriority[0].count").value(1))
                .andExpect(jsonPath("$.data.memberCount").value(1));
    }

    @Test
    void getAnalytics_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/boards/" + board.getId() + "/analytics"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAnalytics_noAccess_returns403_or404() throws Exception {
        // Create another user
        User otherUser = userRepository.save(User.builder()
                .email("other@example.com")
                .password("password")
                .fullName("Other User")
                .build());
        String otherToken = "Bearer " + jwtService.generateAccessToken(otherUser.getId(), otherUser.getEmail());

        // Try to access the first board (should fail because otherUser is not a member)
        mockMvc.perform(get("/api/boards/" + board.getId() + "/analytics")
                        .header("Authorization", otherToken))
                .andExpect(status().isBadRequest());
    }
}

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

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class InvitationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private BoardMemberRepository boardMemberRepository;

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private JwtService jwtService;

    private String token;
    private User user;
    private Board board;
    private String invitationToken;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .email("invitee@example.com")
                .password("password")
                .fullName("Invitee User")
                .build());

        User inviter = userRepository.save(User.builder()
                .email("inviter@example.com")
                .password("password")
                .fullName("Inviter User")
                .build());

        board = boardRepository.save(Board.builder()
                .title("Shared Board")
                .owner(inviter) // Inviter is the owner
                .build());

        boardMemberRepository.save(BoardMember.builder()
                .board(board)
                .user(inviter)
                .role(BoardMember.Role.OWNER)
                .build());

        invitationToken = UUID.randomUUID().toString();
        invitationRepository.save(Invitation.builder()
                .board(board)
                .email(user.getEmail())
                .token(invitationToken)
                .role(BoardMember.Role.EDITOR)
                .status(Invitation.InvitationStatus.PENDING)
                .invitedBy(inviter)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build());

        token = "Bearer " + jwtService.generateAccessToken(user.getId(), user.getEmail());
    }

    @Test
    void getInvitation_returns200() throws Exception {
        mockMvc.perform(get("/api/invitations/" + invitationToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(user.getEmail()))
                .andExpect(jsonPath("$.data.boardTitle").value(board.getTitle()));
    }

    @Test
    void getInvitation_invalidToken_returns404() throws Exception {
        mockMvc.perform(get("/api/invitations/invalid-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    void acceptInvitation_returns200() throws Exception {
        mockMvc.perform(post("/api/invitations/" + invitationToken + "/accept")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.boardId").value(board.getId()));
    }

    @Test
    void acceptInvitation_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/invitations/" + invitationToken + "/accept"))
                .andExpect(status().isUnauthorized());
    }
}

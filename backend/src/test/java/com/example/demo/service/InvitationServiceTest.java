package com.example.demo.service;

import com.example.demo.dto.InvitationAcceptResponse;
import com.example.demo.dto.InvitationDetailsDto;
import com.example.demo.entity.*;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BoardMemberRepository;
import com.example.demo.repository.InvitationRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class InvitationServiceTest {

    @Mock
    private InvitationRepository invitationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BoardMemberRepository boardMemberRepository;

    @InjectMocks
    private InvitationService invitationService;

    @Test
    void getInvitationDetails_shouldSuccess() {
        Board board = new Board();
        board.setId(1L);
        board.setTitle("B");
        Invitation invitation = Invitation.builder()
                .id(1L)
                .token("tok")
                .email("e")
                .board(board)
                .role(BoardMember.Role.EDITOR)
                .status(Invitation.InvitationStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(invitation));
        when(userRepository.existsByEmail("e")).thenReturn(true);

        InvitationDetailsDto result = invitationService.getInvitationDetails("tok");

        assertEquals("e", result.getEmail());
        assertTrue(result.isUserExists());
    }

    @Test
    void getInvitationDetails_expired_shouldThrow() {
        Invitation invitation = Invitation.builder()
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();
        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(invitation));
        assertThrows(BadRequestException.class, () -> invitationService.getInvitationDetails("tok"));
    }

    @Test
    void getInvitationDetails_notFound_shouldThrow() {
        when(invitationRepository.findByToken("tok")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> invitationService.getInvitationDetails("tok"));
    }

    @Test
    void acceptInvitation_shouldSuccess() {
        Board board = new Board();
        board.setId(1L);
        User user = new User();
        user.setId(2L);
        user.setEmail("e");
        Invitation invitation = Invitation.builder()
                .id(1L)
                .token("tok")
                .email("e")
                .board(board)
                .role(BoardMember.Role.EDITOR)
                .status(Invitation.InvitationStatus.PENDING)
                .build();

        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(invitation));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(boardMemberRepository.existsByBoardIdAndUserId(1L, 2L)).thenReturn(false);

        InvitationAcceptResponse result = invitationService.acceptInvitation("tok", 2L);

        assertEquals(1L, result.getBoardId());
        assertEquals(Invitation.InvitationStatus.ACCEPTED, invitation.getStatus());
        verify(boardMemberRepository).save(any());
        verify(invitationRepository).save(invitation);
    }

    @Test
    void acceptInvitation_declined_shouldThrow() {
        Invitation invitation = Invitation.builder()
                .status(Invitation.InvitationStatus.DECLINED)
                .build();
        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(invitation));
        assertThrows(BadRequestException.class, () -> invitationService.acceptInvitation("tok", 1L));
    }

    @Test
    void acceptInvitation_wrongEmail_shouldThrow() {
        User user = new User();
        user.setEmail("wrong");
        Invitation invitation = Invitation.builder()
                .email("right")
                .build();
        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(invitation));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        assertThrows(BadRequestException.class, () -> invitationService.acceptInvitation("tok", 1L));
    }
}

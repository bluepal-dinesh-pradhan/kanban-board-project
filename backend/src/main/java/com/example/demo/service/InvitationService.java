package com.example.demo.service;

import com.example.demo.dto.InvitationAcceptResponse;
import com.example.demo.dto.InvitationDetailsDto;
import com.example.demo.entity.BoardMember;
import com.example.demo.entity.Invitation;
import com.example.demo.entity.User;
import com.example.demo.repository.BoardMemberRepository;
import com.example.demo.repository.InvitationRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final BoardMemberRepository boardMemberRepository;

    @Transactional(readOnly = true)
    public InvitationDetailsDto getInvitationDetails(String token) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        if (invitation.getExpiresAt() != null && invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invitation has expired");
        }

        boolean userExists = userRepository.existsByEmail(invitation.getEmail());

        return new InvitationDetailsDto(
                invitation.getEmail(),
                invitation.getBoard().getId(),
                invitation.getBoard().getTitle(),
                invitation.getRole().name(),
                invitation.getStatus().name(),
                invitation.getExpiresAt(),
                userExists
        );
    }

    @Transactional
    public InvitationAcceptResponse acceptInvitation(String token, Long userId) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        if (invitation.getExpiresAt() != null && invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invitation has expired");
        }

        if (invitation.getStatus() == Invitation.InvitationStatus.DECLINED) {
            throw new RuntimeException("Invitation has been declined");
        }

        User user = userRepository.findById(userId).orElseThrow();
        if (!user.getEmail().equalsIgnoreCase(invitation.getEmail())) {
            throw new RuntimeException("This invitation was sent to a different email");
        }

        if (!boardMemberRepository.existsByBoardIdAndUserId(invitation.getBoard().getId(), user.getId())) {
            BoardMember member = BoardMember.builder()
                    .board(invitation.getBoard())
                    .user(user)
                    .role(invitation.getRole())
                    .build();
            boardMemberRepository.save(member);
        }

        if (invitation.getStatus() != Invitation.InvitationStatus.ACCEPTED) {
            invitation.setStatus(Invitation.InvitationStatus.ACCEPTED);
            invitationRepository.save(invitation);
        }

        return new InvitationAcceptResponse(invitation.getBoard().getId());
    }
}

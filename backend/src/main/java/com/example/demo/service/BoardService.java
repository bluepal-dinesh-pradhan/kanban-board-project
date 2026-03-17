package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final BoardColumnRepository boardColumnRepository;
    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;
    private final ActivityService activityService;
    private final EmailService emailService;

    @Value("${app.invitation.expires-days:14}")
    private long invitationExpiresDays;

    @Transactional
    public BoardDto create(BoardRequest req, Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        Board board = Board.builder()
                .title(req.getTitle())
                .owner(user)
                .background(req.getBackground() != null ? req.getBackground() : "#0079BF")
                .build();
        board = boardRepository.save(board);

        // Add owner as member
        BoardMember member = BoardMember.builder()
                .board(board).user(user).role(BoardMember.Role.OWNER).build();
        boardMemberRepository.save(member);

        // Create default columns
        String[] defaults = {"To Do", "In Progress", "Done"};
        for (int i = 0; i < defaults.length; i++) {
            BoardColumn col = BoardColumn.builder()
                    .board(board).title(defaults[i]).position(i).build();
            boardColumnRepository.save(col);
        }

        activityService.log(board, user, "CREATED_BOARD", "BOARD", board.getId());
        return BoardDto.from(board, "OWNER");
    }

    public List<BoardDto> getUserBoards(Long userId) {
        return boardRepository.findAllByMemberUserId(userId).stream()
                .map(b -> {
                    BoardMember m = boardMemberRepository.findByBoardIdAndUserId(b.getId(), userId).orElseThrow();
                    return BoardDto.from(b, m.getRole().name());
                }).collect(Collectors.toList());
    }

    @Transactional
    public InviteResponse inviteMember(Long boardId, InviteRequest req, Long inviterId) {
        Board board = boardRepository.findById(boardId).orElseThrow();
        BoardMember inviterMember = boardMemberRepository.findByBoardIdAndUserId(boardId, inviterId)
                .orElseThrow(() -> new RuntimeException("Access denied"));
        if (inviterMember.getRole() != BoardMember.Role.OWNER) {
            throw new RuntimeException("Only board owner can invite members");
        }

        User inviter = userRepository.findById(inviterId).orElseThrow();

        if (board.getOwner().getEmail().equalsIgnoreCase(req.getEmail())) {
            throw new RuntimeException("You are already the owner of this board");
        }

        var userOpt = userRepository.findByEmail(req.getEmail());

        if (userOpt.isPresent()) {
            User invitee = userOpt.get();
            if (boardMemberRepository.existsByBoardIdAndUserId(boardId, invitee.getId())) {
                throw new RuntimeException("This user is already a member of this board");
            }

            BoardMember member = BoardMember.builder()
                    .board(board)
                    .user(invitee)
                    .role(req.getRole())
                    .build();
            boardMemberRepository.save(member);

            boolean emailSent = emailService.sendBoardInvitation(
                req.getEmail(),
                inviter.getFullName(),
                board.getTitle(),
                req.getRole().name(),
                true,
                null
            );

            activityService.log(board, inviter, "INVITED_MEMBER", "USER", invitee.getId());

            return InviteResponse.builder()
                .status("ADDED")
                .message("Member added!")
                .emailSent(emailSent)
                .build();
        }

        Invitation existingInvitation = invitationRepository.findByBoardIdAndEmail(boardId, req.getEmail()).orElse(null);
        if (existingInvitation != null) {
            if (existingInvitation.getStatus() == Invitation.InvitationStatus.PENDING) {
                existingInvitation.setToken(UUID.randomUUID().toString());
                existingInvitation.setExpiresAt(LocalDateTime.now().plusDays(invitationExpiresDays));
                existingInvitation.setInvitedBy(inviter);
                existingInvitation.setRole(req.getRole());
                invitationRepository.save(existingInvitation);

                boolean emailSent = emailService.sendBoardInvitation(
                    req.getEmail(),
                    inviter.getFullName(),
                    board.getTitle(),
                    req.getRole().name(),
                    false,
                    existingInvitation.getToken()
                );

                activityService.log(board, inviter, "RESENT_INVITATION", "INVITATION", existingInvitation.getId());

                return InviteResponse.builder()
                    .status("RESENT")
                    .message("Invitation resent!")
                    .emailSent(emailSent)
                    .build();
            }

            if (existingInvitation.getStatus() == Invitation.InvitationStatus.ACCEPTED) {
                throw new RuntimeException("This user is already a member of this board");
            }

            if (existingInvitation.getStatus() == Invitation.InvitationStatus.DECLINED) {
                existingInvitation.setStatus(Invitation.InvitationStatus.PENDING);
                existingInvitation.setToken(UUID.randomUUID().toString());
                existingInvitation.setExpiresAt(LocalDateTime.now().plusDays(invitationExpiresDays));
                existingInvitation.setInvitedBy(inviter);
                existingInvitation.setRole(req.getRole());
                invitationRepository.save(existingInvitation);

                boolean emailSent = emailService.sendBoardInvitation(
                    req.getEmail(),
                    inviter.getFullName(),
                    board.getTitle(),
                    req.getRole().name(),
                    false,
                    existingInvitation.getToken()
                );

                activityService.log(board, inviter, "RESENT_INVITATION", "INVITATION", existingInvitation.getId());

                return InviteResponse.builder()
                    .status("INVITED")
                    .message("Invitation sent!")
                    .emailSent(emailSent)
                    .build();
            }
        }

        Invitation invitation = Invitation.builder()
                .board(board)
                .email(req.getEmail())
                .role(req.getRole())
                .invitedBy(inviter)
                .status(Invitation.InvitationStatus.PENDING)
                .token(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusDays(invitationExpiresDays))
                .build();
        invitationRepository.save(invitation);

        boolean emailSent = emailService.sendBoardInvitation(
            req.getEmail(),
            inviter.getFullName(),
            board.getTitle(),
            req.getRole().name(),
            false,
            invitation.getToken()
        );

        activityService.log(board, inviter, "INVITED_MEMBER", "INVITATION", invitation.getId());

        return InviteResponse.builder()
            .status("INVITED")
            .message("Invitation sent!")
            .emailSent(emailSent)
            .build();
    }

    @Transactional(readOnly = true)
    public List<ColumnDto> getBoardColumns(Long boardId, Long userId) {
        checkAccess(boardId, userId);
        return boardColumnRepository.findByBoardIdAndArchivedFalseOrderByPositionAsc(boardId)
                .stream().map(ColumnDto::from).collect(Collectors.toList());
    }

    public BoardMembersDto getBoardMembers(Long boardId, Long userId) {
        checkAccess(boardId, userId);
        Board board = boardRepository.findById(boardId).orElseThrow();
        
        List<BoardMemberDto> members = board.getMembers().stream()
                .map(BoardMemberDto::from)
                .collect(Collectors.toList());
                
        List<InvitationDto> pendingInvitations = invitationRepository.findByBoardId(boardId)
                .stream()
                .filter(inv -> inv.getStatus() == Invitation.InvitationStatus.PENDING)
                .map(InvitationDto::from)
                .collect(Collectors.toList());
                
        return new BoardMembersDto(members, pendingInvitations);
    }

    @Transactional
    public void removeMember(Long boardId, Long memberId, Long userId) {
        checkPermission(boardId, userId, BoardMember.Role.OWNER);
        BoardMember member = boardMemberRepository.findByIdAndBoardId(memberId, boardId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        if (member.getRole() == BoardMember.Role.OWNER) {
            throw new RuntimeException("Board owner cannot be removed");
        }
        if (member.getUser().getId().equals(userId)) {
            throw new RuntimeException("You cannot remove yourself");
        }

        Board board = member.getBoard();
        User actor = userRepository.findById(userId).orElseThrow();
        User removedUser = member.getUser();

        boardMemberRepository.delete(member);
        invitationRepository.findByBoardIdAndEmailAndStatus(boardId, removedUser.getEmail(), Invitation.InvitationStatus.ACCEPTED)
                .ifPresent(invitationRepository::delete);
        activityService.log(board, actor, "REMOVED_MEMBER", "USER", removedUser.getId());
    }

    @Transactional
    public BoardDto updateBoard(Long boardId, BoardUpdateRequest req, Long userId) {
        checkPermission(boardId, userId, BoardMember.Role.OWNER);
        Board board = boardRepository.findById(boardId).orElseThrow();
        
        if (req.getTitle() != null) {
            board.setTitle(req.getTitle());
        }
        if (req.getBackground() != null) {
            board.setBackground(req.getBackground());
        }
        
        board = boardRepository.save(board);
        BoardMember member = boardMemberRepository.findByBoardIdAndUserId(boardId, userId).orElseThrow();
        return BoardDto.from(board, member.getRole().name());
    }

    @Transactional
    public void cancelInvitation(Long boardId, Long invitationId, Long userId) {
        checkPermission(boardId, userId, BoardMember.Role.OWNER);
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));
        
        if (!invitation.getBoard().getId().equals(boardId)) {
            throw new RuntimeException("Invitation does not belong to this board");
        }
        
        invitationRepository.delete(invitation);
    }

    public void checkAccess(Long boardId, Long userId) {
        if (!boardMemberRepository.existsByBoardIdAndUserId(boardId, userId)) {
            throw new RuntimeException("Access denied to this board");
        }
    }

    public void checkPermission(Long boardId, Long userId, BoardMember.Role minRole) {
        BoardMember m = boardMemberRepository.findByBoardIdAndUserId(boardId, userId)
                .orElseThrow(() -> new RuntimeException("Access denied"));
        if (minRole == BoardMember.Role.OWNER && m.getRole() != BoardMember.Role.OWNER) {
            throw new RuntimeException("Only board owner can perform this action");
        }
        if (minRole == BoardMember.Role.EDITOR && m.getRole() == BoardMember.Role.VIEWER) {
            throw new RuntimeException("Viewers cannot perform this action");
        }
    }
}

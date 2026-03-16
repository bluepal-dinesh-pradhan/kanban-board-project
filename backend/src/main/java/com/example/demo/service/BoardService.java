package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final BoardColumnRepository boardColumnRepository;
    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;
    private final ActivityService activityService;
    private final EmailService emailService;

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
        checkPermission(boardId, inviterId, BoardMember.Role.OWNER);
        User inviter = userRepository.findById(inviterId).orElseThrow();

        // Check if already a member
        if (boardMemberRepository.existsByBoardIdAndUserId(boardId, 
                userRepository.findByEmail(req.getEmail()).map(User::getId).orElse(-1L))) {
            throw new RuntimeException("User is already a member");
        }

        // Check if invitation already exists
        if (invitationRepository.existsByBoardIdAndEmail(boardId, req.getEmail())) {
            throw new RuntimeException("User is already invited");
        }

        // Try to find user by email
        var userOpt = userRepository.findByEmail(req.getEmail());
        
        if (userOpt.isPresent()) {
            // User exists - add directly as member
            User invitee = userOpt.get();
            BoardMember member = BoardMember.builder()
                    .board(board).user(invitee).role(req.getRole()).build();
            boardMemberRepository.save(member);
            
            // Send email notification
            boolean emailSent = emailService.sendBoardInvitation(
                req.getEmail(), 
                inviter.getFullName(), 
                board.getTitle(), 
                req.getRole().name(), 
                true
            );
            
            activityService.log(board, inviter, "INVITED_MEMBER", "USER", invitee.getId());
            
            return InviteResponse.builder()
                .status("ADDED")
                .message(invitee.getFullName() + " has been added to the board!")
                .emailSent(emailSent)
                .build();
        } else {
            // User doesn't exist - create pending invitation
            Invitation invitation = Invitation.builder()
                    .board(board)
                    .email(req.getEmail())
                    .role(req.getRole())
                    .invitedBy(inviter)
                    .status(Invitation.InvitationStatus.PENDING)
                    .build();
            invitationRepository.save(invitation);
            
            // Send email notification
            boolean emailSent = emailService.sendBoardInvitation(
                req.getEmail(), 
                inviter.getFullName(), 
                board.getTitle(), 
                req.getRole().name(), 
                false
            );
            
            activityService.log(board, inviter, "SENT_INVITATION", "INVITATION", invitation.getId());
            
            if (emailSent) {
                return InviteResponse.builder()
                    .status("INVITED")
                    .message("Invitation email sent to " + req.getEmail())
                    .emailSent(true)
                    .build();
            } else {
                return InviteResponse.builder()
                    .status("INVITED")
                    .message("Invitation saved. Share the registration link with them manually.")
                    .emailSent(false)
                    .build();
            }
        }
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

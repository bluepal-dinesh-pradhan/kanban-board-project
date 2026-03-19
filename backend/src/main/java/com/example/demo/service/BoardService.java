package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

@Service @RequiredArgsConstructor
@Slf4j
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final BoardColumnRepository boardColumnRepository;
    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;
    private final NotificationRepository notificationRepository;
    private final CardReminderRepository cardReminderRepository;
    private final CommentRepository commentRepository;
    private final CardLabelRepository cardLabelRepository;
    private final ActivityRepository activityRepository;
    private final CardRepository cardRepository;
    private final ActivityService activityService;
    private final EmailService emailService;

    @Value("${app.invitation.expires-days:14}")
    private long invitationExpiresDays;

    @Transactional
    public BoardDto create(BoardRequest req, Long userId) {
        log.info("Creating board '{}' for user {}", req.getTitle(), userId);
        User user = userRepository.findById(userId).orElseThrow();
        Board board = Board.builder()
                .title(req.getTitle())
                .owner(user)
                .background(req.getBackground() != null ? req.getBackground() : "#0079BF")
                .build();
        board = boardRepository.save(board);
        log.debug("Board persisted with id {}", board.getId());

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
        log.debug("Default columns created for board {}", board.getId());

        activityService.log(board, user, "CREATED_BOARD", "BOARD", board.getId());
        BoardDto dto = BoardDto.from(board, "OWNER");
        log.info("Board '{}' created successfully with id {}", board.getTitle(), board.getId());
        return dto;
    }

    public List<BoardDto> getUserBoards(Long userId) {
        log.info("Fetching boards for user {}", userId);
        List<BoardDto> boards = boardRepository.findAllByMemberUserId(userId).stream()
                .map(b -> {
                    BoardMember m = boardMemberRepository.findByBoardIdAndUserId(b.getId(), userId).orElseThrow();
                    return BoardDto.from(b, m.getRole().name());
                }).collect(Collectors.toList());
        log.debug("Fetching {} boards for user {}", boards.size(), userId);
        log.info("Boards fetched successfully for user {}", userId);
        return boards;
    }

    public PageResponse<BoardDto> getUserBoards(Long userId, int page, int size) {
        log.info("Fetching boards for user {} with page {} and size {}", userId, page, size);
        Page<Board> boards = boardRepository.findAllByMemberUserId(userId, PageRequest.of(page, size));
        List<BoardDto> content = boards.stream()
                .map(b -> {
                    BoardMember m = boardMemberRepository.findByBoardIdAndUserId(b.getId(), userId).orElseThrow();
                    return BoardDto.from(b, m.getRole().name());
                }).collect(Collectors.toList());
        PageResponse<BoardDto> response = new PageResponse<>(
                content,
                boards.getNumber(),
                boards.getSize(),
                boards.getTotalElements(),
                boards.getTotalPages(),
                boards.hasNext(),
                boards.hasPrevious()
        );
        log.debug("Fetching {} boards for user {} (page {}, size {})", content.size(), userId, page, size);
        log.info("Boards fetched successfully for user {} with page {} and size {}", userId, page, size);
        return response;
    }

    @Transactional
        public InviteResponse inviteMember(Long boardId, InviteRequest req, Long inviterId) {
            log.info("Inviting member {} to board {} by user {}", req.getEmail(), boardId, inviterId);
            Board board = boardRepository.findById(boardId).orElseThrow();
            checkPermission(boardId, inviterId, BoardMember.Role.OWNER);
            User inviter = userRepository.findById(inviterId).orElseThrow();

            // Check if already a member
            if (boardMemberRepository.existsByBoardIdAndUserId(boardId, 
                    userRepository.findByEmail(req.getEmail()).map(User::getId).orElse(-1L))) {
                log.warn("User {} already a member of board {}", req.getEmail(), boardId);
                throw new RuntimeException("User is already a member");
            }

            // Check if invitation already exists
            if (invitationRepository.existsByBoardIdAndEmail(boardId, req.getEmail())) {
                log.warn("User {} already invited to board {}", req.getEmail(), boardId);
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

                InviteResponse response = InviteResponse.builder()
                    .status("ADDED")
                    .message(invitee.getFullName() + " has been added to the board!")
                    .emailSent(emailSent)
                    .build();
                log.info("Member {} added to board {} successfully", invitee.getId(), boardId);
                return response;
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
                    InviteResponse response = InviteResponse.builder()
                        .status("INVITED")
                        .message("Invitation email sent to " + req.getEmail())
                        .emailSent(true)
                        .build();
                    log.info("Invitation email sent to {} for board {}", req.getEmail(), boardId);
                    return response;
                } else {
                    InviteResponse response = InviteResponse.builder()
                        .status("INVITED")
                        .message("Invitation saved. Share the registration link with them manually.")
                        .emailSent(false)
                        .build();
                    log.warn("Invitation saved without email for {} on board {}", req.getEmail(), boardId);
                    return response;
                }
            }
        }

    @Transactional(readOnly = true)
    public List<ColumnDto> getBoardColumns(Long boardId, Long userId) {
        log.info("Fetching columns for board {} and user {}", boardId, userId);
        checkAccess(boardId, userId);
        List<ColumnDto> columns = boardColumnRepository.findByBoardIdAndArchivedFalseOrderByPositionAsc(boardId)
                .stream().map(ColumnDto::from).collect(Collectors.toList());
        log.debug("Fetched {} columns for board {}", columns.size(), boardId);
        log.info("Columns fetched successfully for board {}", boardId);
        return columns;
    }

    @Transactional(readOnly = true)
    public PageResponse<ColumnDto> getBoardColumns(Long boardId, Long userId, int page, int size) {
        log.info("Fetching columns for board {} with page {} and size {}", boardId, page, size);
        checkAccess(boardId, userId);
        Page<BoardColumn> columns = boardColumnRepository.findByBoardIdAndArchivedFalse(
                boardId,
                PageRequest.of(page, size, Sort.by("position").ascending())
        );
        PageResponse<ColumnDto> response = PageResponse.from(columns.map(ColumnDto::from));
        log.debug("Fetched {} columns for board {} (page {}, size {})", response.getContent().size(), boardId, page, size);
        log.info("Columns fetched successfully for board {} with page {} and size {}", boardId, page, size);
        return response;
    }

    public BoardMembersDto getBoardMembers(Long boardId, Long userId) {
        log.info("Fetching board members for board {} and user {}", boardId, userId);
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

        BoardMembersDto dto = new BoardMembersDto(members, pendingInvitations);
        log.debug("Fetched {} members and {} pending invitations for board {}", members.size(), pendingInvitations.size(), boardId);
        log.info("Board members fetched successfully for board {}", boardId);
        return dto;
    }

    @Transactional
    public void removeMember(Long boardId, Long memberId, Long userId) {
        log.info("Removing member {} from board {} by user {}", memberId, boardId, userId);
        checkPermission(boardId, userId, BoardMember.Role.OWNER);
        BoardMember member = boardMemberRepository.findByIdAndBoardId(memberId, boardId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        if (member.getRole() == BoardMember.Role.OWNER) {
            log.warn("User {} attempted to remove board owner {} from board {}", userId, memberId, boardId);
            throw new RuntimeException("Board owner cannot be removed");
        }
        if (member.getUser().getId().equals(userId)) {
            log.warn("User {} attempted to remove themselves from board {}", userId, boardId);
            throw new RuntimeException("You cannot remove yourself");
        }

        Board board = member.getBoard();
        User actor = userRepository.findById(userId).orElseThrow();
        User removedUser = member.getUser();

        boardMemberRepository.delete(member);
        invitationRepository.findByBoardIdAndEmailAndStatus(boardId, removedUser.getEmail(), Invitation.InvitationStatus.ACCEPTED)
                .ifPresent(invitationRepository::delete);
        activityService.log(board, actor, "REMOVED_MEMBER", "USER", removedUser.getId());
        log.info("Member {} removed from board {}", memberId, boardId);
    }

    @Transactional
    public void deleteBoard(Long boardId, Long userId) {
        log.info("Deleting board {} by user {}", boardId, userId);
        checkPermission(boardId, userId, BoardMember.Role.OWNER);
        if (!boardRepository.existsById(boardId)) {
            throw new RuntimeException("Board not found");
        }

        // Delete related data in FK-safe order
        notificationRepository.deleteByBoardId(boardId);
        cardReminderRepository.deleteByBoardId(boardId);
        commentRepository.deleteByBoardId(boardId);
        cardLabelRepository.deleteByBoardId(boardId);
        activityRepository.deleteByBoardId(boardId);
        cardRepository.deleteByBoardId(boardId);
        boardColumnRepository.deleteByBoardId(boardId);
        invitationRepository.deleteByBoardId(boardId);
        boardMemberRepository.deleteByBoardId(boardId);
        boardRepository.deleteById(boardId);

        log.info("Board {} and all related data deleted successfully", boardId);
    }

    @Transactional
    public BoardDto updateBoard(Long boardId, BoardUpdateRequest req, Long userId) {
        log.info("Updating board {} by user {}", boardId, userId);
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
        BoardDto dto = BoardDto.from(board, member.getRole().name());
        log.info("Board {} updated successfully", boardId);
        return dto;
    }

    @Transactional
    public void cancelInvitation(Long boardId, Long invitationId, Long userId) {
        log.info("Cancelling invitation {} for board {} by user {}", invitationId, boardId, userId);
        checkPermission(boardId, userId, BoardMember.Role.OWNER);
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));
        
        if (!invitation.getBoard().getId().equals(boardId)) {
            log.warn("Invitation {} does not belong to board {}", invitationId, boardId);
            throw new RuntimeException("Invitation does not belong to this board");
        }
        
        invitationRepository.delete(invitation);
        log.info("Invitation {} cancelled successfully for board {}", invitationId, boardId);
    }

    public void checkAccess(Long boardId, Long userId) {
        if (!boardMemberRepository.existsByBoardIdAndUserId(boardId, userId)) {
            log.warn("User {} attempted to access board {} without permission", userId, boardId);
            throw new RuntimeException("Access denied to this board");
        }
    }

    public void checkPermission(Long boardId, Long userId, BoardMember.Role minRole) {
        BoardMember m = boardMemberRepository.findByBoardIdAndUserId(boardId, userId)
                .orElseThrow(() -> {
                    log.warn("User {} attempted to access board {} without permission", userId, boardId);
                    return new RuntimeException("Access denied");
                });
        if (minRole == BoardMember.Role.OWNER && m.getRole() != BoardMember.Role.OWNER) {
            log.warn("User {} attempted owner-only action on board {}", userId, boardId);
            throw new RuntimeException("Only board owner can perform this action");
        }
        if (minRole == BoardMember.Role.EDITOR && m.getRole() == BoardMember.Role.VIEWER) {
            log.warn("User {} attempted editor action on board {}", userId, boardId);
            throw new RuntimeException("Viewers cannot perform this action");
        }
    }

    public boolean isOwner(Long boardId, Long userId) {
        return boardMemberRepository.findByBoardIdAndUserId(boardId, userId)
                .map(m -> m.getRole() == BoardMember.Role.OWNER)
                .orElse(false);
    }
}

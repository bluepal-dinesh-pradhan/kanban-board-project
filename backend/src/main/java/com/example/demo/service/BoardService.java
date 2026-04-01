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
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.BadRequestException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
    private final StarredBoardRepository starredBoardRepository;

    @Value("${app.invitation.expires-days:14}")
    private long invitationExpiresDays;

    private static final String TO_DO = "To Do";
    private static final String IN_PROGRESS = "In Progress";
    private static final String DONE = "Done";

    private static final String BOARD_NOT_FOUND = "Board not found";
    private static final String USER_NOT_FOUND = "User not found";
    private static final String ACCESS_DENIED = "Access denied";
    private static final String MEMBER_NOT_FOUND = "Member not found";

    @Transactional
    public BoardDto create(BoardRequest req, Long userId) {
        log.info("Creating board '{}' for user {}", req.getTitle(), userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        Board board = Board.builder()
                .title(req.getTitle())
                .owner(user)
                .background(req.getBackground() != null ? req.getBackground() : "#0079BF")
                .build();
        board = boardRepository.save(board);
        log.debug("Board persisted with id {}", board.getId());

        BoardMember member = BoardMember.builder()
                .board(board).user(user).role(BoardMember.Role.OWNER).build();
        boardMemberRepository.save(member);

        String[] defaults = {TO_DO, IN_PROGRESS, DONE};
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
                    BoardMember m = boardMemberRepository.findByBoardIdAndUserId(b.getId(), userId)
                            .orElseThrow(() -> new ResourceNotFoundException(MEMBER_NOT_FOUND));
                    return BoardDto.from(b, m.getRole().name());
                }).toList();
        log.debug("Fetching {} boards for user {}", boards.size(), userId);
        log.info("Boards fetched successfully for user {}", userId);
        return boards;
    }

    public PageResponse<BoardDto> getUserBoards(Long userId, int page, int size) {
        log.info("Fetching boards for user {} with page {} and size {}", userId, page, size);
        Page<Board> boards = boardRepository.findAllByMemberUserId(userId, PageRequest.of(page, size));
        List<BoardDto> content = boards.stream()
                .map(b -> {
                    BoardMember m = boardMemberRepository.findByBoardIdAndUserId(b.getId(), userId)
                            .orElseThrow(() -> new ResourceNotFoundException(MEMBER_NOT_FOUND));
                    return BoardDto.from(b, m.getRole().name());
                }).toList();
        PageResponse<BoardDto> response = new PageResponse<>(
                content, boards.getNumber(), boards.getSize(),
                boards.getTotalElements(), boards.getTotalPages(),
                boards.hasNext(), boards.hasPrevious()
        );
        log.debug("Fetching {} boards for user {} (page {}, size {})", content.size(), userId, page, size);
        log.info("Boards fetched successfully for user {} with page {} and size {}", userId, page, size);
        return response;
    }

    public BoardDto getBoard(Long boardId, Long userId) {
        log.info("Fetching board {} for user {}", boardId, userId);
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException(BOARD_NOT_FOUND));
        BoardMember member = boardMemberRepository.findByBoardIdAndUserId(boardId, userId)
                .orElseThrow(() -> {
                    log.warn("Access denied for user {} to board {}", userId, boardId);
                    return new BadRequestException(ACCESS_DENIED);
                });
        BoardDto dto = BoardDto.from(board, member.getRole().name());
        log.info("Board {} fetched successfully", boardId);
        return dto;
    }

    @Transactional
    public InviteResponse inviteMember(Long boardId, InviteRequest req, Long inviterId) {
        log.info("Inviting member {} to board {} by user {}", req.getEmail(), boardId, inviterId);
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException(BOARD_NOT_FOUND));
        checkPermission(boardId, inviterId, BoardMember.Role.OWNER);
        User inviter = userRepository.findById(inviterId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        if (boardMemberRepository.existsByBoardIdAndUserId(boardId,
                userRepository.findByEmail(req.getEmail()).map(User::getId).orElse(-1L))) {
            log.warn("User {} already a member of board {}", req.getEmail(), boardId);
            throw new BadRequestException("User is already a member");
        }

        if (invitationRepository.existsByBoardIdAndEmail(boardId, req.getEmail())) {
            log.warn("User {} already invited to board {}", req.getEmail(), boardId);
            throw new BadRequestException("User is already invited");
        }

        var userOpt = userRepository.findByEmail(req.getEmail());

        if (userOpt.isPresent()) {
            User invitee = userOpt.get();
            BoardMember member = BoardMember.builder()
                    .board(board).user(invitee).role(req.getRole()).build();
            boardMemberRepository.save(member);

            boolean emailSent = emailService.sendBoardInvitation(
                    req.getEmail(), inviter.getFullName(), board.getTitle(),
                    req.getRole().name(), true);

            activityService.log(board, inviter, "INVITED_MEMBER", "USER", invitee.getId());

            InviteResponse response = InviteResponse.builder()
                    .status("ADDED")
                    .message(invitee.getFullName() + " has been added to the board!")
                    .emailSent(emailSent).build();
            log.info("Member {} added to board {} successfully", invitee.getId(), boardId);
            return response;
        } else {
            Invitation invitation = Invitation.builder()
                    .board(board).email(req.getEmail()).role(req.getRole())
                    .invitedBy(inviter).status(Invitation.InvitationStatus.PENDING).build();
            invitationRepository.save(invitation);

            boolean emailSent = emailService.sendBoardInvitation(
                    req.getEmail(), inviter.getFullName(), board.getTitle(),
                    req.getRole().name(), false);

            activityService.log(board, inviter, "SENT_INVITATION", "INVITATION", invitation.getId());

            if (emailSent) {
                return InviteResponse.builder().status("INVITED")
                        .message("Invitation email sent to " + req.getEmail())
                        .emailSent(true).build();
            } else {
                return InviteResponse.builder().status("INVITED")
                        .message("Invitation saved. Share the registration link with them manually.")
                        .emailSent(false).build();
            }
        }
    }

    @Transactional(readOnly = true)
    public List<ColumnDto> getBoardColumns(Long boardId, Long userId) {
        log.info("Fetching columns for board {} and user {}", boardId, userId);
        checkAccess(boardId, userId);
        List<ColumnDto> columns = boardColumnRepository.findByBoardIdAndArchivedFalseOrderByPositionAsc(boardId)
                .stream().map(ColumnDto::from).toList();
        log.debug("Fetched {} columns for board {}", columns.size(), boardId);
        log.info("Columns fetched successfully for board {}", boardId);
        return columns;
    }

    @Transactional(readOnly = true)
    public PageResponse<ColumnDto> getBoardColumns(Long boardId, Long userId, int page, int size) {
        log.info("Fetching columns for board {} with page {} and size {}", boardId, page, size);
        checkAccess(boardId, userId);
        Page<BoardColumn> columns = boardColumnRepository.findByBoardIdAndArchivedFalse(
                boardId, PageRequest.of(page, size, Sort.by("position").ascending()));
        PageResponse<ColumnDto> response = PageResponse.from(columns.map(ColumnDto::from));
        log.debug("Fetched {} columns for board {} (page {}, size {})", response.getContent().size(), boardId, page, size);
        log.info("Columns fetched successfully for board {} with page {} and size {}", boardId, page, size);
        return response;
    }

    public BoardMembersDto getBoardMembers(Long boardId, Long userId) {
        log.info("Fetching board members for board {} and user {}", boardId, userId);
        checkAccess(boardId, userId);
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException(BOARD_NOT_FOUND));

        List<BoardMemberDto> members = board.getMembers().stream()
                .map(BoardMemberDto::from).toList();

        List<InvitationDto> pendingInvitations = invitationRepository.findByBoardId(boardId)
                .stream()
                .filter(inv -> inv.getStatus() == Invitation.InvitationStatus.PENDING)
                .map(InvitationDto::from).toList();

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
                .orElseThrow(() -> new ResourceNotFoundException(MEMBER_NOT_FOUND));

        if (member.getRole() == BoardMember.Role.OWNER) {
            throw new BadRequestException("Board owner cannot be removed");
        }
        if (member.getUser().getId().equals(userId)) {
            throw new BadRequestException("You cannot remove yourself");
        }

        Board board = member.getBoard();
        User actor = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
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
            throw new ResourceNotFoundException(BOARD_NOT_FOUND);
        }

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
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException(BOARD_NOT_FOUND));

        if (req.getTitle() != null) board.setTitle(req.getTitle());
        if (req.getBackground() != null) board.setBackground(req.getBackground());

        board = boardRepository.save(board);
        BoardMember member = boardMemberRepository.findByBoardIdAndUserId(boardId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(MEMBER_NOT_FOUND));
        BoardDto dto = BoardDto.from(board, member.getRole().name());
        log.info("Board {} updated successfully", boardId);
        return dto;
    }

    @Transactional
    public boolean toggleStar(Long boardId, Long userId) {
        log.info("Toggling star for board {} by user {}", boardId, userId);
        checkAccess(boardId, userId);

        Optional<StarredBoard> existing = starredBoardRepository.findByUserIdAndBoardId(userId, boardId);

        if (existing.isPresent()) {
            starredBoardRepository.delete(existing.get());
            log.info("Board {} unstarred by user {}", boardId, userId);
            return false;
        } else {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
            Board board = boardRepository.findById(boardId)
                    .orElseThrow(() -> new ResourceNotFoundException(BOARD_NOT_FOUND));
            StarredBoard star = StarredBoard.builder().user(user).board(board).build();
            starredBoardRepository.save(star);
            log.info("Board {} starred by user {}", boardId, userId);
            return true;
        }
    }

    public boolean isStarred(Long boardId, Long userId) {
        return starredBoardRepository.existsByUserIdAndBoardId(userId, boardId);
    }

    public List<Long> getStarredBoardIds(Long userId) {
        return starredBoardRepository.findByUserIdOrderByStarredAtDesc(userId)
                .stream().map(s -> s.getBoard().getId()).toList();
    }

    @Transactional
    public BoardDto createFromTemplate(String title, String background, String templateType, Long userId) {
        log.info("Creating board '{}' from template '{}' for user {}", title, templateType, userId);

        List<String> columns;
        switch (templateType.toUpperCase()) {
            case "SCRUM":
                columns = List.of("Backlog", "Sprint", IN_PROGRESS, "Review", DONE);
                break;
            case "BUG_TRACKER":
                columns = List.of("New", "Triaging", IN_PROGRESS, "Testing", "Resolved", "Closed");
                break;
            case "MARKETING":
                columns = List.of("Ideas", "Planning", IN_PROGRESS, "Review", "Published");
                break;
            case "PERSONAL":
                columns = List.of(TO_DO, "Doing", DONE);
                break;
            case "DESIGN":
                columns = List.of("Research", "Wireframes", "Design", "Feedback", "Final");
                break;
            default:
                columns = List.of(TO_DO, IN_PROGRESS, DONE);
                break;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        Board board = Board.builder()
                .title(title)
                .background(background != null ? background : "#0079BF")
                .owner(user).build();
        board = boardRepository.save(board);

        BoardMember member = BoardMember.builder()
                .board(board).user(user).role(BoardMember.Role.OWNER).build();
        boardMemberRepository.save(member);

        for (int i = 0; i < columns.size(); i++) {
            BoardColumn col = BoardColumn.builder()
                    .board(board).title(columns.get(i)).position(i).build();
            boardColumnRepository.save(col);
        }

        activityService.log(board, user, "CREATED_BOARD_FROM_TEMPLATE", "BOARD", board.getId());

        log.info("Board '{}' created from template '{}' with {} columns", title, templateType, columns.size());
        return BoardDto.from(board, "OWNER");
    }

    @Transactional
    public void cancelInvitation(Long boardId, Long invitationId, Long userId) {
        log.info("Cancelling invitation {} for board {} by user {}", invitationId, boardId, userId);
        checkPermission(boardId, userId, BoardMember.Role.OWNER);
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        if (!invitation.getBoard().getId().equals(boardId)) {
            throw new BadRequestException("Invitation does not belong to this board");
        }

        invitationRepository.delete(invitation);
        log.info("Invitation {} cancelled successfully for board {}", invitationId, boardId);
    }

    public void checkAccess(Long boardId, Long userId) {
        if (!boardMemberRepository.existsByBoardIdAndUserId(boardId, userId)) {
            log.warn("User {} attempted to access board {} without permission", userId, boardId);
            throw new BadRequestException("Access denied to this board");
        }
    }

    public void checkPermission(Long boardId, Long userId, BoardMember.Role minRole) {
        BoardMember m = boardMemberRepository.findByBoardIdAndUserId(boardId, userId)
                .orElseThrow(() -> {
                    log.warn("User {} attempted to access board {} without permission", userId, boardId);
                    return new BadRequestException(ACCESS_DENIED);
                });
        if (minRole == BoardMember.Role.OWNER && m.getRole() != BoardMember.Role.OWNER) {
            throw new BadRequestException("Only board owner can perform this action");
        }
        if (minRole == BoardMember.Role.EDITOR && m.getRole() == BoardMember.Role.VIEWER) {
            throw new BadRequestException("Viewers cannot perform this action");
        }
    }

    public boolean isOwner(Long boardId, Long userId) {
        return boardMemberRepository.findByBoardIdAndUserId(boardId, userId)
                .map(m -> m.getRole() == BoardMember.Role.OWNER).orElse(false);
    }
}
package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.BadRequestException;

import java.util.List;
import java.util.Map;


@Service @RequiredArgsConstructor
@Slf4j
public class CardService {

    private final CardRepository cardRepository;
    private final CardLabelRepository cardLabelRepository;
    private final BoardColumnRepository columnRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BoardService boardService;
    private final ActivityService activityService;
    private final ReminderService reminderService;
    private final WebSocketNotificationService webSocketNotificationService;

    private static final String EVENT_CARD_CREATED = "card.created";
    private static final String EVENT_CARD_UPDATED = "card.updated";
    private static final String CARD_ENTITY = "CARD";

    private static final String CARD_NOT_FOUND = "Card not found";
    private static final String COLUMN_NOT_FOUND = "Column not found";
    private static final String BOARD_NOT_FOUND = "Board not found";
    private static final String USER_NOT_FOUND = "User not found";

    @Transactional
    public CardDto create(Long boardId, CardRequest req, Long userId) {
        log.info("Creating card '{}' on board {} for user {}", req.getTitle(), boardId, userId);
        boardService.checkPermission(boardId, userId, BoardMember.Role.EDITOR);
        BoardColumn col = columnRepository.findById(req.getColumnId())
                .orElseThrow(() -> new ResourceNotFoundException(COLUMN_NOT_FOUND));
        int pos = cardRepository.countByColumnIdAndArchivedFalse(col.getId());
        log.debug("Creating card at position {} in column {}", pos, col.getId());

        Card card = Card.builder()
                .column(col).title(req.getTitle()).description(req.getDescription())
                .dueDate(req.getDueDate()).position(pos).build();

        // NEW: Set priority
        if (req.getPriority() != null) {
            try {
                card.setPriority(Priority.valueOf(req.getPriority()));
            } catch (IllegalArgumentException e) {
                card.setPriority(Priority.NONE);
            }
        }

        card = cardRepository.save(card);

        if (req.getLabels() != null) {
            for (LabelDto ld : req.getLabels()) {
                CardLabel label = CardLabel.builder().card(card).color(ld.getColor()).text(ld.getText()).build();
                cardLabelRepository.save(label);
                card.getLabels().add(label);
            }
        }
        log.debug("Added {} labels to card {}", card.getLabels().size(), card.getId());

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException(BOARD_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        if (req.getDueDate() != null && req.getReminderType() != null) {
            try {
                CardReminder.ReminderType reminderType = CardReminder.ReminderType.valueOf(req.getReminderType());
                reminderService.createReminder(card, reminderType);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid reminder type '{}' for card {}", req.getReminderType(), card.getId());
            }
        }

        activityService.log(board, user, "CREATED_CARD", CARD_ENTITY, card.getId());
        CardDto dto = CardDto.from(card);
        log.info("Card '{}' created successfully with id {}", card.getTitle(), card.getId());

        broadcastSafely(boardId, EVENT_CARD_CREATED, userId, user.getFullName(), dto);

        return dto;
    }

    @Transactional
    public CardDto update(Long cardId, CardRequest req, Long userId) {
        log.info("Updating card {} by user {}", cardId, userId);
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException(CARD_NOT_FOUND));
        Long boardId = card.getColumn().getBoard().getId();
        boardService.checkPermission(boardId, userId, BoardMember.Role.EDITOR);
        log.debug("Updating card {} in board {}", cardId, boardId);

        card.setTitle(req.getTitle());
        card.setDescription(req.getDescription());
        card.setDueDate(req.getDueDate());

        // NEW: Update priority
        if (req.getPriority() != null) {
            try {
                card.setPriority(Priority.valueOf(req.getPriority()));
            } catch (IllegalArgumentException e) {
                card.setPriority(Priority.NONE);
            }
        }

        cardLabelRepository.deleteAllByCardId(cardId);
        card.getLabels().clear();
        if (req.getLabels() != null) {
            for (LabelDto ld : req.getLabels()) {
                CardLabel label = CardLabel.builder().card(card).color(ld.getColor()).text(ld.getText()).build();
                cardLabelRepository.save(label);
                card.getLabels().add(label);
            }
        }
        log.debug("Updated labels for card {}. Label count={}", cardId, card.getLabels().size());

        card = cardRepository.save(card);
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException(BOARD_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        if (req.getDueDate() != null && req.getReminderType() != null) {
            try {
                CardReminder.ReminderType reminderType = CardReminder.ReminderType.valueOf(req.getReminderType());
                reminderService.createReminder(card, reminderType);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid reminder type '{}' for card {}", req.getReminderType(), cardId);
            }
        } else if (req.getDueDate() == null) {
            reminderService.deleteCardReminders(cardId);
        }

        activityService.log(board, user, "UPDATED_CARD", CARD_ENTITY, card.getId());
        CardDto dto = CardDto.from(card);
        log.info("Card {} updated successfully", cardId);

        broadcastSafely(boardId, EVENT_CARD_UPDATED, userId, user.getFullName(), dto);

        return dto;
    }

    // NEW: Quick priority update endpoint
    @Transactional
    public CardDto updatePriority(Long cardId, String priorityStr, Long userId) {
        log.info("Updating priority of card {} to {} by user {}", cardId, priorityStr, userId);
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException(CARD_NOT_FOUND));
        Long boardId = card.getColumn().getBoard().getId();
        boardService.checkPermission(boardId, userId, BoardMember.Role.EDITOR);

        try {
            card.setPriority(Priority.valueOf(priorityStr));
        } catch (IllegalArgumentException e) {
            card.setPriority(Priority.NONE);
        }
        card = cardRepository.save(card);

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException(BOARD_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        activityService.log(board, user, "SET_PRIORITY_" + priorityStr, CARD_ENTITY, cardId);

        CardDto dto = CardDto.from(card);
        log.info("Card {} priority updated to {}", cardId, priorityStr);

        broadcastSafely(boardId, EVENT_CARD_UPDATED, userId, user.getFullName(), dto);

        return dto;
    }
    
    @Transactional
    public CardDto assignCard(Long cardId, Long assigneeId, Long userId) {
        log.info("Assigning card {} to user {} by user {}", cardId, assigneeId, userId);
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException(CARD_NOT_FOUND));
        Long boardId = card.getColumn().getBoard().getId();
        boardService.checkPermission(boardId, userId, BoardMember.Role.EDITOR);

        if (assigneeId != null) {
            // Verify assignee is a board member
            User assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
            boardService.checkAccess(boardId, assigneeId); // Ensures they're a member
            card.setAssignee(assignee);
        } else {
            // Unassign
            card.setAssignee(null);
        }

        card = cardRepository.save(card);

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException(BOARD_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        String action = assigneeId != null ? "ASSIGNED_CARD" : "UNASSIGNED_CARD";
        activityService.log(board, user, action, CARD_ENTITY, cardId);

        CardDto dto = CardDto.from(card);
        log.info("Card {} assignee updated successfully", cardId);

        broadcastSafely(boardId, EVENT_CARD_UPDATED, userId, user.getFullName(), dto);

        return dto;
    }
    
    
    @Transactional
    public CardDto duplicate(Long cardId, Long userId) {
        log.info("Duplicating card {} by user {}", cardId, userId);
        Card original = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException(CARD_NOT_FOUND));
        Long boardId = original.getColumn().getBoard().getId();
        boardService.checkPermission(boardId, userId, BoardMember.Role.EDITOR);

        int pos = cardRepository.countByColumnIdAndArchivedFalse(original.getColumn().getId());

        Card copy = Card.builder()
                .column(original.getColumn())
                .title(original.getTitle() + " (copy)")
                .description(original.getDescription())
                .dueDate(original.getDueDate())
                .priority(original.getPriority())
                .position(pos)
                .build();
        copy = cardRepository.save(copy);

        // Copy labels
        for (CardLabel label : original.getLabels()) {
            CardLabel newLabel = CardLabel.builder()
                    .card(copy).color(label.getColor()).text(label.getText()).build();
            cardLabelRepository.save(newLabel);
            copy.getLabels().add(newLabel);
        }

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException(BOARD_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        activityService.log(board, user, "DUPLICATED_CARD", CARD_ENTITY, copy.getId());

        CardDto dto = CardDto.from(copy);
        log.info("Card {} duplicated as card {}", cardId, copy.getId());

        broadcastSafely(boardId, EVENT_CARD_CREATED, userId, user.getFullName(), dto);

        return dto;
    }

    @Transactional
    public CardDto restore(Long cardId, Long userId) {
        log.info("Restoring card {} by user {}", cardId, userId);
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException(CARD_NOT_FOUND));
        Long boardId = card.getColumn().getBoard().getId();
        boardService.checkPermission(boardId, userId, BoardMember.Role.EDITOR);

        if (!card.isArchived()) {
            throw new BadRequestException("Card is not archived");
        }

        card.setArchived(false);
        int pos = cardRepository.countByColumnIdAndArchivedFalse(card.getColumn().getId());
        card.setPosition(pos);
        card = cardRepository.save(card);

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException(BOARD_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        activityService.log(board, user, "RESTORED_CARD", CARD_ENTITY, card.getId());

        CardDto dto = CardDto.from(card);
        log.info("Card {} restored successfully", cardId);

        broadcastSafely(boardId, EVENT_CARD_CREATED, userId, user.getFullName(), dto);

        return dto;
    }

    public List<CardDto> getArchivedCards(Long boardId, Long userId) {
        log.info("Fetching archived cards for board {} by user {}", boardId, userId);
        boardService.checkAccess(boardId, userId);
        List<Card> cards = cardRepository.findByColumnBoardIdAndArchivedTrue(boardId);
        return cards.stream().map(CardDto::from).toList();
    }
    
    
    

    @Transactional
    public CardDto move(Long cardId, MoveCardRequest req, Long userId) {
        log.info("Moving card {} to column {} position {} by user {}", cardId, req.getTargetColumnId(), req.getNewPosition(), userId);
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException(CARD_NOT_FOUND));
        Long boardId = card.getColumn().getBoard().getId();
        boardService.checkPermission(boardId, userId, BoardMember.Role.EDITOR);

        Long sourceColumnId = card.getColumn().getId();
        log.debug("Card {} current column {} position {}", cardId, sourceColumnId, card.getPosition());

        BoardColumn sourceCol = card.getColumn();
        BoardColumn targetCol = columnRepository.findById(req.getTargetColumnId())
                .orElseThrow(() -> new ResourceNotFoundException(COLUMN_NOT_FOUND));

        cardRepository.decrementPositionsAfter(sourceCol.getId(), card.getPosition());
        cardRepository.incrementPositionsFrom(targetCol.getId(), req.getNewPosition());
        card.setColumn(targetCol);
        card.setPosition(req.getNewPosition());
        card = cardRepository.save(card);

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException(BOARD_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        activityService.log(board, user, "MOVED_CARD", CARD_ENTITY, card.getId());
        CardDto dto = CardDto.from(card);
        log.info("Card {} moved successfully", cardId);

        Map<String, Object> movePayload = Map.of(
            "card", dto,
            "sourceColumnId", sourceColumnId,
            "targetColumnId", req.getTargetColumnId(),
            "newPosition", req.getNewPosition()
        );
        broadcastSafely(boardId, "card.moved", userId, user.getFullName(), movePayload);

        return dto;
    }

    public CardDto getCard(Long cardId, Long userId) {
        log.info("Fetching card {} for user {}", cardId, userId);
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException(CARD_NOT_FOUND));
        boardService.checkAccess(card.getColumn().getBoard().getId(), userId);
        log.debug("Card {} belongs to board {}", cardId, card.getColumn().getBoard().getId());
        CardDto dto = CardDto.from(card);
        log.info("Card {} fetched successfully", cardId);
        return dto;
    }

    @Transactional
    public CardDto archive(Long cardId, Long userId) {
        log.info("Archiving card {} by user {}", cardId, userId);
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException(CARD_NOT_FOUND));
        Long boardId = card.getColumn().getBoard().getId();
        boardService.checkPermission(boardId, userId, BoardMember.Role.EDITOR);
        log.debug("Archiving card {} in board {}", cardId, boardId);

        card.setArchived(true);
        card = cardRepository.save(card);

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException(BOARD_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        activityService.log(board, user, "ARCHIVED_CARD", CARD_ENTITY, card.getId());
        CardDto dto = CardDto.from(card);
        log.info("Card {} archived successfully", cardId);

        broadcastSafely(boardId, "card.archived", userId, user.getFullName(), dto);

        return dto;
    }

    public List<CommentDto> getComments(Long cardId) {
        log.info("Fetching comments for card {}", cardId);
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException(CARD_NOT_FOUND));
        List<CommentDto> comments = card.getComments().stream().map(CommentDto::from).toList();
        log.debug("Fetching {} comments for card {}", comments.size(), cardId);
        log.info("Comments fetched successfully for card {}", cardId);
        return comments;
    }

    public PageResponse<CommentDto> getComments(Long cardId, int page, int size) {
        log.info("Fetching comments for card {} with page {} and size {}", cardId, page, size);
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException(CARD_NOT_FOUND));
        Page<Comment> comments = commentRepository.findByCardIdOrderByCreatedAtDesc(
                card.getId(),
                PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
        PageResponse<CommentDto> response = PageResponse.from(comments.map(CommentDto::from));
        log.debug("Fetching {} comments for card {} (page {}, size {})", response.getContent().size(), cardId, page, size);
        log.info("Comments fetched successfully for card {} with page {} and size {}", cardId, page, size);
        return response;
    }

    @Transactional
    public void delete(Long cardId, Long userId) {
        log.info("Deleting card {} by user {}", cardId, userId);
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
        Long boardId = card.getColumn().getBoard().getId();
        Long columnId = card.getColumn().getId();
        int position = card.getPosition();
        boolean archived = card.isArchived();
        String title = card.getTitle();

        boardService.checkPermission(boardId, userId, BoardMember.Role.EDITOR);

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException(BOARD_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        commentRepository.deleteByCardId(cardId);
        cardLabelRepository.deleteAllByCardId(cardId);
        reminderService.deleteCardReminders(cardId);

        activityService.log(board, user, "Deleted card: " + title, CARD_ENTITY, cardId);
        cardRepository.delete(card);

        if (!archived) {
            cardRepository.decrementPositionsAfter(columnId, position);
        }

        log.info("Card {} deleted successfully", cardId);

        Map<String, Object> deletePayload = Map.of(
            "cardId", cardId,
            "columnId", columnId,
            "title", title
        );
        broadcastSafely(boardId, "card.deleted", userId, user.getFullName(), deletePayload);
    }

    private void broadcastSafely(Long boardId, String eventType, Long userId, String userName, Object payload) {
        try {
            webSocketNotificationService.broadcastBoardEvent(boardId, eventType, userId, userName, payload);
        } catch (Exception e) {
            log.warn("WebSocket broadcast failed for {} on board {}: {}", eventType, boardId, e.getMessage());
        }
    }
}
package com.example.demo.service;

import com.example.demo.dto.CommentDto;
import com.example.demo.dto.CommentRequest;
import com.example.demo.entity.*;
import com.example.demo.repository.CardRepository;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.BadRequestException;


import java.util.List;
import java.util.Map;


@Service @RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final BoardService boardService;
    private final ActivityService activityService;
    private final WebSocketNotificationService webSocketNotificationService; // NEW

    @Transactional
    public CommentDto addComment(Long cardId, CommentRequest req, Long userId) {
        log.info("Adding comment to card {} by user {}", cardId, userId);
        log.debug("Comment length for card {} is {}", cardId, req.getContent() != null ? req.getContent().length() : 0);
        Card card = cardRepository.findById(cardId).orElseThrow();
        Board board = card.getColumn().getBoard();
        boardService.checkPermission(board.getId(), userId, BoardMember.Role.VIEWER);

        User user = userRepository.findById(userId).orElseThrow();
        Comment comment = Comment.builder()
                .card(card).user(user).content(req.getContent()).build();
        comment = commentRepository.save(comment);

        activityService.log(board, user, "ADDED_COMMENT", "CARD", cardId);
        CommentDto dto = CommentDto.from(comment);
        log.info("Comment {} added successfully to card {}", comment.getId(), cardId);

        // NEW: Broadcast real-time event
        broadcastSafely(board.getId(), "comment.added", userId, user.getFullName(),
            Map.of("cardId", cardId, "comment", dto));

        return dto;
    }

    public List<CommentDto> getComments(Long cardId) {
        log.info("Fetching comments for card {}", cardId);
        Card card = cardRepository.findById(cardId).orElseThrow();
        return card.getComments().stream().map(CommentDto::from).toList();
    }

    @Transactional
    public void deleteComment(Long boardId, Long cardId, Long commentId, Long userId) {
        log.info("Deleting comment {} for card {} on board {} by user {}", commentId, cardId, boardId, userId);
        boardService.checkAccess(boardId, userId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (!comment.getCard().getId().equals(cardId)) {
            throw new BadRequestException("Comment does not belong to this card");
        }

        boolean isCommentAuthor = comment.getUser().getId().equals(userId);
        boolean isBoardOwner = boardService.isOwner(boardId, userId);

        if (!isCommentAuthor && !isBoardOwner) {
            log.warn("User {} attempted to delete someone else's comment {}", userId, commentId);
            throw new BadRequestException("You can only delete your own comments");
        }

        // Get user name before deleting
        User user = userRepository.findById(userId).orElseThrow();

        commentRepository.delete(comment);
        log.info("Comment {} deleted successfully by user {}", commentId, userId);

        // NEW: Broadcast real-time event
        broadcastSafely(boardId, "comment.deleted", userId, user.getFullName(),
            Map.of("cardId", cardId, "commentId", commentId));
    }

    // NEW: Safe WebSocket broadcast helper
    private void broadcastSafely(Long boardId, String eventType, Long userId, String userName, Object payload) {
        try {
            webSocketNotificationService.broadcastBoardEvent(boardId, eventType, userId, userName, payload);
        } catch (Exception e) {
            log.warn("WebSocket broadcast failed for {} on board {}: {}", eventType, boardId, e.getMessage());
        }
    }
}
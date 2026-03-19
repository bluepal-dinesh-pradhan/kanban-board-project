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

import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final BoardService boardService;
    private final ActivityService activityService;

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
        log.info("Comment {} added successfully to card {}", comment.getId(), cardId);
        return CommentDto.from(comment);
    }

    public List<CommentDto> getComments(Long cardId) {
        log.info("Fetching comments for card {}", cardId);
        Card card = cardRepository.findById(cardId).orElseThrow();
        return card.getComments().stream().map(CommentDto::from).collect(Collectors.toList());
    }

    @Transactional
    public void deleteComment(Long boardId, Long cardId, Long commentId, Long userId) {
        log.info("Deleting comment {} for card {} on board {} by user {}", commentId, cardId, boardId, userId);
        boardService.checkAccess(boardId, userId);
        
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        
        if (!comment.getCard().getId().equals(cardId)) {
            throw new RuntimeException("Comment does not belong to this card");
        }

        boolean isCommentAuthor = comment.getUser().getId().equals(userId);
        boolean isBoardOwner = boardService.isOwner(boardId, userId);
        
        if (!isCommentAuthor && !isBoardOwner) {
            log.warn("User {} attempted to delete someone else's comment {}", userId, commentId);
            throw new RuntimeException("You can only delete your own comments");
        }
        
        commentRepository.delete(comment);
        log.info("Comment {} deleted successfully by user {}", commentId, userId);
    }
}

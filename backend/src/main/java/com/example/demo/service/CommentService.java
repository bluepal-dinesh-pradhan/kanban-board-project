package com.example.demo.service;

import com.example.demo.dto.CommentDto;
import com.example.demo.dto.CommentRequest;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final BoardService boardService;
    private final ActivityService activityService;

    @Transactional
    public CommentDto addComment(Long cardId, CommentRequest req, Long userId) {
        Card card = cardRepository.findById(cardId).orElseThrow();
        Board board = card.getColumn().getBoard();
        boardService.checkPermission(board.getId(), userId, BoardMember.Role.EDITOR);

        User user = userRepository.findById(userId).orElseThrow();
        Comment comment = Comment.builder()
                .card(card).user(user).content(req.getContent()).build();
        comment = commentRepository.save(comment);

        activityService.log(board, user, "ADDED_COMMENT", "CARD", cardId);
        return CommentDto.from(comment);
    }
}
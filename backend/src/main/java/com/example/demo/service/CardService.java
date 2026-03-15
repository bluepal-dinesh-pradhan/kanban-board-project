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
public class CardService {

    private final CardRepository cardRepository;
    private final CardLabelRepository cardLabelRepository;
    private final BoardColumnRepository columnRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardService boardService;
    private final ActivityService activityService;

    @Transactional
    public CardDto create(Long boardId, CardRequest req, Long userId) {
        boardService.checkPermission(boardId, userId, BoardMember.Role.EDITOR);
        BoardColumn col = columnRepository.findById(req.getColumnId()).orElseThrow();
        int pos = cardRepository.countByColumnIdAndArchivedFalse(col.getId());

        Card card = Card.builder()
                .column(col).title(req.getTitle()).description(req.getDescription())
                .dueDate(req.getDueDate()).position(pos).build();
        card = cardRepository.save(card);

        if (req.getLabels() != null) {
            for (LabelDto ld : req.getLabels()) {
                CardLabel label = CardLabel.builder().card(card).color(ld.getColor()).text(ld.getText()).build();
                cardLabelRepository.save(label);
                card.getLabels().add(label);
            }
        }

        Board board = boardRepository.findById(boardId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();
        activityService.log(board, user, "CREATED_CARD", "CARD", card.getId());
        return CardDto.from(card);
    }

    @Transactional
    public CardDto update(Long cardId, CardRequest req, Long userId) {
        Card card = cardRepository.findById(cardId).orElseThrow();
        Long boardId = card.getColumn().getBoard().getId();
        boardService.checkPermission(boardId, userId, BoardMember.Role.EDITOR);

        card.setTitle(req.getTitle());
        card.setDescription(req.getDescription());
        card.setDueDate(req.getDueDate());

        // Update labels
        cardLabelRepository.deleteAllByCardId(cardId);
        card.getLabels().clear();
        if (req.getLabels() != null) {
            for (LabelDto ld : req.getLabels()) {
                CardLabel label = CardLabel.builder().card(card).color(ld.getColor()).text(ld.getText()).build();
                cardLabelRepository.save(label);
                card.getLabels().add(label);
            }
        }

        card = cardRepository.save(card);
        Board board = boardRepository.findById(boardId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();
        activityService.log(board, user, "UPDATED_CARD", "CARD", card.getId());
        return CardDto.from(card);
    }

    @Transactional
    public CardDto move(Long cardId, MoveCardRequest req, Long userId) {
        Card card = cardRepository.findById(cardId).orElseThrow();
        Long boardId = card.getColumn().getBoard().getId();
        boardService.checkPermission(boardId, userId, BoardMember.Role.EDITOR);

        BoardColumn sourceCol = card.getColumn();
        BoardColumn targetCol = columnRepository.findById(req.getTargetColumnId()).orElseThrow();

        // Remove from source
        cardRepository.decrementPositionsAfter(sourceCol.getId(), card.getPosition());

        // Insert into target
        cardRepository.incrementPositionsFrom(targetCol.getId(), req.getNewPosition());
        card.setColumn(targetCol);
        card.setPosition(req.getNewPosition());
        card = cardRepository.save(card);

        Board board = boardRepository.findById(boardId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();
        activityService.log(board, user, "MOVED_CARD", "CARD", card.getId());
        return CardDto.from(card);
    }

    public CardDto getCard(Long cardId, Long userId) {
        Card card = cardRepository.findById(cardId).orElseThrow();
        boardService.checkAccess(card.getColumn().getBoard().getId(), userId);
        return CardDto.from(card);
    }

    public List<CommentDto> getComments(Long cardId) {
        Card card = cardRepository.findById(cardId).orElseThrow();
        return card.getComments().stream().map(CommentDto::from).collect(Collectors.toList());
    }
}

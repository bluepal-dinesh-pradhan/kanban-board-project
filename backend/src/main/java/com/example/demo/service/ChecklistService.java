package com.example.demo.service;

import com.example.demo.dto.ChecklistDto;
import com.example.demo.entity.*;
import com.example.demo.repository.CardRepository;
import com.example.demo.repository.ChecklistRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.BadRequestException;


import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
public class ChecklistService {

    private final ChecklistRepository checklistRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final BoardService boardService;
    private final ActivityService activityService;
    private final WebSocketNotificationService webSocketNotificationService;

    private static final String CHECKLIST_ITEM_NOT_FOUND = "Checklist item not found";
    private static final String PAYLOAD_CARD_ID = "cardId";
    private static final String PAYLOAD_CHECKLIST = "checklist";
    private static final String EVENT_CARD_UPDATED = "card.updated";

    public List<ChecklistDto> getItems(Long cardId) {
        return checklistRepository.findByCardIdOrderByPositionAsc(cardId)
                .stream().map(ChecklistDto::from).toList();
    }

    @Transactional
    public ChecklistDto addItem(Long cardId, String title, Long userId) {
        log.info("Adding checklist item '{}' to card {} by user {}", title, cardId, userId);
        Card card = cardRepository.findById(cardId).orElseThrow();
        Long boardId = card.getColumn().getBoard().getId();
        boardService.checkPermission(boardId, userId, BoardMember.Role.EDITOR);

        int pos = checklistRepository.countByCardId(cardId);

        Checklist item = Checklist.builder()
                .card(card)
                .title(title)
                .position(pos)
                .build();
        item = checklistRepository.save(item);

        User user = userRepository.findById(userId).orElseThrow();
        activityService.log(card.getColumn().getBoard(), user, "ADDED_CHECKLIST_ITEM", "CARD", cardId);

        ChecklistDto dto = ChecklistDto.from(item);
        log.info("Checklist item '{}' added to card {}", title, cardId);

        broadcastSafely(boardId, EVENT_CARD_UPDATED, userId, user.getFullName(),
                Map.of(PAYLOAD_CARD_ID, cardId, PAYLOAD_CHECKLIST, "item_added"));

        return dto;
    }

    @Transactional
    public ChecklistDto toggleItem(Long itemId, Long userId) {
        log.info("Toggling checklist item {} by user {}", itemId, userId);
        Checklist item = checklistRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(CHECKLIST_ITEM_NOT_FOUND));
        Long boardId = item.getCard().getColumn().getBoard().getId();
        boardService.checkPermission(boardId, userId, BoardMember.Role.EDITOR);

        item.setCompleted(!item.isCompleted());
        item = checklistRepository.save(item);

        User user = userRepository.findById(userId).orElseThrow();
        ChecklistDto dto = ChecklistDto.from(item);
        log.info("Checklist item {} toggled to {}", itemId, item.isCompleted());

        broadcastSafely(boardId, EVENT_CARD_UPDATED, userId, user.getFullName(),
                Map.of(PAYLOAD_CARD_ID, item.getCard().getId(), PAYLOAD_CHECKLIST, "item_toggled"));

        return dto;
    }

    @Transactional
    public ChecklistDto updateItem(Long itemId, String title, Long userId) {
        log.info("Updating checklist item {} by user {}", itemId, userId);
        Checklist item = checklistRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(CHECKLIST_ITEM_NOT_FOUND));
        Long boardId = item.getCard().getColumn().getBoard().getId();
        boardService.checkPermission(boardId, userId, BoardMember.Role.EDITOR);

        item.setTitle(title);
        item = checklistRepository.save(item);

        log.info("Checklist item {} updated", itemId);
        return ChecklistDto.from(item);
    }

    @Transactional
    public void deleteItem(Long itemId, Long userId) {
        log.info("Deleting checklist item {} by user {}", itemId, userId);
        Checklist item = checklistRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(CHECKLIST_ITEM_NOT_FOUND));
        Long boardId = item.getCard().getColumn().getBoard().getId();
        Long cardId = item.getCard().getId();
        boardService.checkPermission(boardId, userId, BoardMember.Role.EDITOR);

        checklistRepository.delete(item);

        User user = userRepository.findById(userId).orElseThrow();
        log.info("Checklist item {} deleted", itemId);

        broadcastSafely(boardId, EVENT_CARD_UPDATED, userId, user.getFullName(),
                Map.of(PAYLOAD_CARD_ID, cardId, PAYLOAD_CHECKLIST, "item_deleted"));
    }

    /**
     * Returns checklist progress for a card: [completed, total]
     */
    public int[] getProgress(Long cardId) {
        int total = checklistRepository.countByCardId(cardId);
        int completed = checklistRepository.countByCardIdAndCompletedTrue(cardId);
        return new int[]{completed, total};
    }

    private void broadcastSafely(Long boardId, String eventType, Long userId, String userName, Object payload) {
        try {
            webSocketNotificationService.broadcastBoardEvent(boardId, eventType, userId, userName, payload);
        } catch (Exception e) {
            log.warn("WebSocket broadcast failed for {} on board {}: {}", eventType, boardId, e.getMessage());
        }
    }
}
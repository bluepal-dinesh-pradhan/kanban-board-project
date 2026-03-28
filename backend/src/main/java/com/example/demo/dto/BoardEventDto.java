package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardEventDto {

    private String eventType;      // e.g., "card.created", "card.moved", "column.deleted"
    private Long boardId;
    private Long userId;           // who triggered the event
    private String userName;       // full name of the user who triggered it
    private Object payload;        // the actual data (CardDto, ColumnDto, etc.)
    private LocalDateTime timestamp;

    /**
     * Factory method for quick creation.
     * Automatically sets timestamp to now.
     */
    public static BoardEventDto of(String eventType, Long boardId, Long userId, String userName, Object payload) {
        return BoardEventDto.builder()
                .eventType(eventType)
                .boardId(boardId)
                .userId(userId)
                .userName(userName)
                .payload(payload)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
package com.example.demo.dto;

import com.example.demo.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor
public class NotificationDto {
    private Long id;
    private String title;
    private String message;
    private String type;
    private boolean isRead;
    private LocalDateTime createdAt;
    private Long cardId;
    private String cardTitle;
    private Long boardId;
    private String boardTitle;

    public static NotificationDto from(Notification notification) {
        return new NotificationDto(
            notification.getId(),
            notification.getTitle(),
            notification.getMessage(),
            notification.getType().name(),
            notification.isRead(),
            notification.getCreatedAt(),
            notification.getCard().getId(),
            notification.getCard().getTitle(),
            notification.getCard().getColumn().getBoard().getId(),
            notification.getCard().getColumn().getBoard().getTitle()
        );
    }
}
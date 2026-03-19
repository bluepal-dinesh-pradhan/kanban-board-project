package com.example.demo.dto;

import com.example.demo.entity.Notification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "Notification details.")
@Data @AllArgsConstructor @NoArgsConstructor
public class NotificationDto {
    @Schema(description = "Notification id.")
    private Long id;
    @Schema(description = "Notification title.")
    private String title;
    @Schema(description = "Notification message.")
    private String message;
    @Schema(description = "Notification type.")
    private String type;
    @Schema(description = "Whether the notification has been read.")
    private boolean isRead;
    @Schema(description = "Notification creation timestamp.")
    private LocalDateTime createdAt;
    @Schema(description = "Related card id, if applicable.")
    private Long cardId;
    @Schema(description = "Related card title, if applicable.")
    private String cardTitle;
    @Schema(description = "Related board id, if applicable.")
    private Long boardId;
    @Schema(description = "Related board title, if applicable.")
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

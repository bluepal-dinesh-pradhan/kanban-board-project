package com.example.demo.dto;

import com.example.demo.entity.CardReminder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "Card reminder details.")
@Data @AllArgsConstructor @NoArgsConstructor
public class CardReminderDto {
    @Schema(description = "Reminder id.")
    private Long id;
    @Schema(description = "Reminder type.")
    private String reminderType;
    @Schema(description = "When the reminder should trigger.")
    private LocalDateTime reminderDateTime;
    @Schema(description = "Whether the reminder has been triggered.")
    private boolean triggered;

    public static CardReminderDto from(CardReminder reminder) {
        return new CardReminderDto(
            reminder.getId(),
            reminder.getReminderType().name(),
            reminder.getReminderDateTime(),
            reminder.isTriggered()
        );
    }
}

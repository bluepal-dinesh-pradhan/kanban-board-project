package com.example.demo.dto;

import com.example.demo.entity.CardReminder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor
public class CardReminderDto {
    private Long id;
    private String reminderType;
    private LocalDateTime reminderDateTime;
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

package com.example.demo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.example.demo.entity.Card;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class CardDto {
    private Long id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private int position;
    private Long columnId;
    private List<LabelDto> labels;
    private List<CardReminderDto> reminders;
    private int commentCount;
    private LocalDateTime createdAt;
    private boolean isOverdue;

    public static CardDto from(Card c) {
        List<LabelDto> labels = c.getLabels().stream()
                .map(l -> new LabelDto(l.getId(), l.getColor(), l.getText()))
                .collect(Collectors.toList());
        
        List<CardReminderDto> reminders = c.getReminders().stream()
                .map(CardReminderDto::from)
                .collect(Collectors.toList());
        
        boolean isOverdue = c.getDueDate() != null && c.getDueDate().isBefore(LocalDate.now());
        
        return new CardDto(c.getId(), c.getTitle(), c.getDescription(),
                c.getDueDate(), c.getPosition(), c.getColumn().getId(),
                labels, reminders, c.getComments().size(), c.getCreatedAt(), isOverdue);
    }
}


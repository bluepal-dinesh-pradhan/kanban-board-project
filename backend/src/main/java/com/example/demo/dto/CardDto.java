package com.example.demo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.example.demo.entity.Card;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Card details.")
@Data @AllArgsConstructor @NoArgsConstructor
public class CardDto {
    @Schema(description = "Card id.")
    private Long id;
    @Schema(description = "Card title.")
    private String title;
    @Schema(description = "Card description.")
    private String description;
    @Schema(description = "Card due date.")
    private LocalDate dueDate;
    @Schema(description = "Card position within its column.")
    private int position;
    @Schema(description = "Column id containing the card.")
    private Long columnId;
    @Schema(description = "Labels attached to the card.")
    private List<LabelDto> labels;
    @Schema(description = "Reminders configured for the card.")
    private List<CardReminderDto> reminders;
    @Schema(description = "Number of comments on the card.")
    private int commentCount;
    @Schema(description = "Card creation timestamp.")
    private LocalDateTime createdAt;
    @Schema(description = "Whether the card is overdue.")
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


package com.example.demo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


import com.example.demo.entity.Card;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Card details.")
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
    private String priority;
    private Long assigneeId;
    private String assigneeName;
    private String assigneeEmail;

    // NEW: Checklist progress
    @Schema(description = "Total checklist items.")
    private int checklistTotal;

    @Schema(description = "Completed checklist items.")
    private int checklistCompleted;

    @Schema(description = "Number of attachments on the card.")
    private int attachmentCount;

    public static CardDto from(Card c) {
        List<LabelDto> labels = c.getLabels().stream()
                .map(l -> new LabelDto(l.getId(), l.getColor(), l.getText()))
                .toList();

        List<CardReminderDto> reminders = c.getReminders().stream()
                .map(CardReminderDto::from)
                .toList();

        boolean isOverdue = c.getDueDate() != null && c.getDueDate().isBefore(LocalDate.now());

        CardDto dto = new CardDto();
        dto.setId(c.getId());
        dto.setTitle(c.getTitle());
        dto.setDescription(c.getDescription());
        dto.setDueDate(c.getDueDate());
        dto.setPosition(c.getPosition());
        dto.setColumnId(c.getColumn().getId());
        dto.setLabels(labels);
        dto.setReminders(reminders);
        dto.setCommentCount(c.getComments().size());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setOverdue(isOverdue);
        dto.setPriority(c.getPriority() != null ? c.getPriority().name() : "NONE");

        // Assignee
        if (c.getAssignee() != null) {
            dto.setAssigneeId(c.getAssignee().getId());
            dto.setAssigneeName(c.getAssignee().getFullName());
            dto.setAssigneeEmail(c.getAssignee().getEmail());
        }

        // NEW: Checklist progress
        if (c.getChecklists() != null && !c.getChecklists().isEmpty()) {
            dto.setChecklistTotal(c.getChecklists().size());
            dto.setChecklistCompleted(
                (int) c.getChecklists().stream().filter(cl -> cl.isCompleted()).count()
            );
        }

        // Attachment count
        if (c.getAttachments() != null) {
            dto.setAttachmentCount(c.getAttachments().size());
        }

        return dto;
    }
}
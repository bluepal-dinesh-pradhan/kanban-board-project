package com.example.demo.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Card creation or update request.")
@Data
public class CardRequest {
    @Schema(description = "Card title.", example = "Implement login flow")
    @NotBlank
    private String title;
    @Schema(description = "Card description.", example = "Add JWT-based auth to the API.")
    private String description;
    @Schema(description = "Card due date.")
    private LocalDate dueDate;
    @Schema(description = "Target column id.")
    private Long columnId;
    @Schema(description = "Labels attached to the card.")
    private List<LabelDto> labels;
    @Schema(description = "Reminder type.", example = "ONE_DAY_BEFORE")
    private String reminderType; // ONE_DAY_BEFORE, TWO_DAYS_BEFORE, ONE_WEEK_BEFORE, AT_DUE_TIME
}

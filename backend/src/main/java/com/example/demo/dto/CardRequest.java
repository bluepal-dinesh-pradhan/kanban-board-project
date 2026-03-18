package com.example.demo.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CardRequest {
    @NotBlank
    private String title;
    private String description;
    private LocalDate dueDate;
    private Long columnId;
    private List<LabelDto> labels;
    private String reminderType; // ONE_DAY_BEFORE, TWO_DAYS_BEFORE, ONE_WEEK_BEFORE, AT_DUE_TIME
}

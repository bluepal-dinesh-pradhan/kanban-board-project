package com.example.demo.dto;

import com.example.demo.entity.Checklist;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "Checklist item details.")
@Data @AllArgsConstructor @NoArgsConstructor
public class ChecklistDto {

    @Schema(description = "Checklist item id.")
    private Long id;

    @Schema(description = "Checklist item title.")
    private String title;

    @Schema(description = "Whether the item is completed.")
    private boolean completed;

    @Schema(description = "Position in the checklist.")
    private int position;

    @Schema(description = "Creation timestamp.")
    private LocalDateTime createdAt;

    public static ChecklistDto from(Checklist c) {
        return new ChecklistDto(
                c.getId(),
                c.getTitle(),
                c.isCompleted(),
                c.getPosition(),
                c.getCreatedAt()
        );
    }
}
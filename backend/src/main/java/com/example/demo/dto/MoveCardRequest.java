package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Move card request.")
@Data
public class MoveCardRequest {
    @Schema(description = "Target column id.")
    @NotNull
    private Long targetColumnId;

    @Schema(description = "New position within the target column.")
    @NotNull
    private Integer newPosition;
}

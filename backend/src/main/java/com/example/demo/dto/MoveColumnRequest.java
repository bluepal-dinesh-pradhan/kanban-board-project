package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Request to move a column to a new position.")
@Data @AllArgsConstructor @NoArgsConstructor
public class MoveColumnRequest {
    @Schema(description = "New index/position for the column.")
    private int newPosition;
}

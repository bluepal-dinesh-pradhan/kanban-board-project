package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MoveCardRequest {
    @NotNull
    private Long targetColumnId;

    @NotNull
    private Integer newPosition;
}

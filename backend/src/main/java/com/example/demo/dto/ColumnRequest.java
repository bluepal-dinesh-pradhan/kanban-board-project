package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Column creation request.")
@Data
public class ColumnRequest {
    @Schema(description = "Column title.", example = "In Progress")
    @NotBlank
    private String title;
}

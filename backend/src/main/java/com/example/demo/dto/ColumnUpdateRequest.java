package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "Column update request.")
@Data
public class ColumnUpdateRequest {
    @Schema(description = "Column title.", example = "Done")
    @NotBlank
    private String title;
}


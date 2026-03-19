package com.example.demo.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Board update request.")
@Data
public class BoardUpdateRequest {
    @Schema(description = "Updated board title.", example = "Engineering Board")
    private String title;
    @Schema(description = "Updated board background (color or image identifier).", example = "#1F2937")
    private String background;
}

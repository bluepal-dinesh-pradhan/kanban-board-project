package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Board creation request.")
@Data
public class BoardRequest {
    @Schema(description = "Board title.", example = "Product Roadmap")
    @NotBlank
    private String title;
    
    @Schema(description = "Board background (color or image identifier).", example = "#0F4C81")
    private String background;
}

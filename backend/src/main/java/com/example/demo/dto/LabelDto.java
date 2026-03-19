package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Label details.")
@Data @AllArgsConstructor @NoArgsConstructor
public class LabelDto {
    @Schema(description = "Label id.")
    private Long id;
    @Schema(description = "Label color.", example = "#FFB100")
    private String color;
    @Schema(description = "Label text.", example = "Backend")
    private String text;
}

package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Comment creation request.")
@Data
public class CommentRequest {
    @Schema(description = "Comment content.", example = "Looks good to me.")
    @NotBlank
    private String content;
}

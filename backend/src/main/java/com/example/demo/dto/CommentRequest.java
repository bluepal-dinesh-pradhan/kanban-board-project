package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentRequest {
    @NotBlank
    private String content;
}
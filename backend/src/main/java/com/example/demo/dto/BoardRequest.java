package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BoardRequest {
    @NotBlank
    private String title;
}

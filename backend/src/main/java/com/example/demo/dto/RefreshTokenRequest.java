package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Refresh token request payload.")
@Data
public class RefreshTokenRequest {
    @Schema(description = "Refresh token previously issued by the system.")
    @NotBlank
    private String refreshToken;
}

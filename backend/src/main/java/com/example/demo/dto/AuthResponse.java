package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response containing tokens and user info.")
@Data @AllArgsConstructor
public class AuthResponse {
    @Schema(description = "JWT access token.")
    private String accessToken;
    @Schema(description = "JWT refresh token.")
    private String refreshToken;
    @Schema(description = "Authenticated user details.")
    private UserDto user;
}

package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Registration request payload.")
@Data
public class RegisterRequest {
    @Schema(description = "User email address.", example = "user@example.com")
    @Email @NotBlank
    private String email;

    @Schema(description = "User password.", example = "P@ssw0rd")
    @NotBlank @Size(min = 6)
    private String password;

    @Schema(description = "Full name of the user.", example = "Jane Doe")
    @NotBlank
    private String fullName;
}

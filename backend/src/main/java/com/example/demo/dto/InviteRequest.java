package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.example.demo.entity.BoardMember;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Invite member request.")
@Data
public class InviteRequest {
    @Schema(description = "Email address to invite.", example = "teammate@example.com")
    @Email @NotBlank
    private String email;

    @Schema(description = "Role for the invited member.", example = "MEMBER")
    @NotNull
    private BoardMember.Role role;
}

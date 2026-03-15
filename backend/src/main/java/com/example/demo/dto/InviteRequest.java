package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.example.demo.entity.BoardMember;
import lombok.Data;

@Data
public class InviteRequest {
    @Email @NotBlank
    private String email;

    @NotNull
    private BoardMember.Role role;
}

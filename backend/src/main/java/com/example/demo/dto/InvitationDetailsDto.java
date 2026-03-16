package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvitationDetailsDto {
    private String email;
    private Long boardId;
    private String boardTitle;
    private String role;
    private String status;
    private LocalDateTime expiresAt;
    private boolean userExists;
}

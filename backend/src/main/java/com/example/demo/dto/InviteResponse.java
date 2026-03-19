package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Invite operation result.")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteResponse {
    @Schema(description = "Invite status.", example = "INVITED")
    private String status;    // "ADDED" or "INVITED"
    @Schema(description = "User-facing message about the invite result.")
    private String message;   // User-friendly message
    @Schema(description = "Whether an email notification was sent.")
    private boolean emailSent; // Whether email was actually delivered
}

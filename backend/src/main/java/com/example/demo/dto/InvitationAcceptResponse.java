package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Invitation acceptance response.")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvitationAcceptResponse {
    @Schema(description = "Board id that the user joined.")
    private Long boardId;
}

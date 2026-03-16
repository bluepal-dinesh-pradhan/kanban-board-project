package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteResponse {
    private String status;    // "ADDED" or "INVITED"
    private String message;   // User-friendly message
    private boolean emailSent; // Whether email was actually delivered
}
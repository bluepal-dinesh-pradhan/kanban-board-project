package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard API response wrapper.")
@Data @AllArgsConstructor
public class ApiResponse<T> {
    @Schema(description = "Indicates if the request was successful.")
    private boolean success;
    @Schema(description = "Human-readable message about the result.")
    private String message;
    @Schema(description = "Response payload.")
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "Success", data);
    }

    public static <T> ApiResponse<T> ok(String msg, T data) {
        return new ApiResponse<>(true, msg, data);
    }

    public static <T> ApiResponse<T> error(String msg) {
        return new ApiResponse<>(false, msg, null);
    }
}

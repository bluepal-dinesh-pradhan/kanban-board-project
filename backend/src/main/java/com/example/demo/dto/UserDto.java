package com.example.demo.dto;

import com.example.demo.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class UserDto {
    private Long id;
    private String email;
    private String fullName;

    public static UserDto from(User u) {
        return new UserDto(u.getId(), u.getEmail(), u.getFullName());
    }
}

package com.akcadag.payload.response;

import com.akcadag.payload.dto.UserDto;
import lombok.Data;

@Data
public class AuthResponse {
    private String jwt;
    private String message;
   // private String title;
    private UserDto user;
}

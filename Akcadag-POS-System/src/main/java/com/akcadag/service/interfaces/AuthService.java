package com.akcadag.service.interfaces;

import com.akcadag.exceptions.UserException;
import com.akcadag.payload.dto.UserDto;
import com.akcadag.payload.response.AuthResponse;

public interface AuthService {
    AuthResponse signUp(UserDto userDto) throws UserException;
    AuthResponse logIn(UserDto userDto) throws UserException;
}

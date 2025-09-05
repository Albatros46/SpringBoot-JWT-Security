package com.akcadag.controller;

import com.akcadag.exceptions.UserException;
import com.akcadag.payload.dto.UserDto;
import com.akcadag.payload.response.AuthResponse;
import com.akcadag.service.interfaces.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signUpHandler(@RequestBody UserDto userDto) throws UserException {
        return ResponseEntity.ok( authService.signUp(userDto));
    }
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> logInHandler(@RequestBody UserDto userDto) throws UserException {
        return ResponseEntity.ok( authService.logIn(userDto));
    }
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = authService.getAllUsers();
        return ResponseEntity.ok(users);
    }

}

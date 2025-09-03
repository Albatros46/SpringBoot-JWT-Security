package com.akcadag.service.impl;

import com.akcadag.configuration.JwtProvider;
import com.akcadag.domain.UserRole;
import com.akcadag.exceptions.UserException;
import com.akcadag.models.User;
import com.akcadag.payload.dto.UserDto;
import com.akcadag.payload.response.AuthResponse;
import com.akcadag.repository.UserRepository;
import com.akcadag.service.interfaces.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final CustomUserImpl customUserImpl;
    @Override
    public AuthResponse signUp(UserDto userDto) throws UserException {
        User user=userRepository.findByEmail(userDto.getEmail());
        if (user!=null){
            throw new UserException("Email Id already registered!");
        }
        if(userDto.getRole().equals(UserRole.ROLE_ADMIN)){
            throw new UserException("Role Admin is not allowed!");
        }
        return null;
    }

    @Override
    public AuthResponse logIn(UserDto userDto) {
        return null;
    }
}

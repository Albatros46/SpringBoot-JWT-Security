package com.akcadag.service.impl;

import com.akcadag.configuration.JwtProvider;
import com.akcadag.exceptions.UserException;
import com.akcadag.models.User;
import com.akcadag.repository.UserRepository;
import com.akcadag.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    /**
     * JWT token'dan kullanıcıyı bulur.
     */
    @Override
    public User getUserFromJwtToken(String token) throws UserException {
        String email = jwtProvider.getEmailFromToken(token);
        User user=userRepository.findByEmail(email);
        if(user==null){
            throw new UserException("Invalid Token!");
        }
        return user;
    }

    /**
     * Şu anda giriş yapmış kullanıcıyı döndürür.
     */
    @Override
    public User getCurrentUser() throws UserException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserException("No authenticated user found!");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email);
    }

    /**
     * Email ile kullanıcıyı bulur.
     */
    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * ID ile kullanıcıyı bulur.
     */
    @Override
    public User getUserById(Long id) throws UserException {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserException("User not found with id: " + id));
    }

    /**
     * Tüm kullanıcıları döndürür.
     */
    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}

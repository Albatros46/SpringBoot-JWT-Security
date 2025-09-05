package com.akcadag.service.interfaces;

import com.akcadag.exceptions.UserException;
import com.akcadag.models.User;

import java.util.List;

public interface UserService {
    User getUserFromJwtToken(String token) throws UserException;
    User getCurrentUser() throws UserException;
    User getUserByEmail(String email);
    User getUserById(Long email) throws UserException;
    List<User> getAllUsers();
}

package com.akcadag.service.impl;

import com.akcadag.configuration.JwtProvider;
import com.akcadag.domain.UserRole;
import com.akcadag.exceptions.UserException;
import com.akcadag.mapper.UserMapper;
import com.akcadag.models.User;
import com.akcadag.payload.dto.UserDto;
import com.akcadag.payload.response.AuthResponse;
import com.akcadag.repository.UserRepository;
import com.akcadag.service.interfaces.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final CustomUserImpl customUserImpl;

    /**
     * Kullanıcı kayıt işlemlerini gerçekleştiren metod.
     * - Email adresi zaten kayıtlı mı kontrol eder.
     * - Admin rolüyle kayıt yapılmasını engeller.
     * - Kullanıcı bilgilerini kaydeder.
     * - JWT token üretir ve AuthResponse olarak döner.
     */
    @Override
    public AuthResponse signUp(UserDto userDto) throws UserException {
        // Eğer email adresi daha önce kullanılmışsa hata fırlatılır
        User user = userRepository.findByEmail(userDto.getEmail());
        if (user != null) {
            throw new UserException("Email Id already registered!");
        }

        // Admin rolüyle kayıt yapılmasına izin verilmez
        if (userDto.getRole().equals(UserRole.ROLE_ADMIN)) {
            throw new UserException("Role Admin is not allowed!");
        }

        // Yeni kullanıcı nesnesi oluşturuluyor
        User newUser = new User();
        newUser.setEmail(userDto.getEmail());
        // Kullanıcı şifresi güvenlik için BCrypt ile hashleniyor
        newUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
        newUser.setRole(userDto.getRole());
        newUser.setFullName(userDto.getFullName());
        newUser.setPhone(userDto.getPhone());
        newUser.setLastLoginAt(LocalDateTime.now());
        newUser.setCreatedAt(LocalDateTime.now());

        // Yeni kullanıcı veritabanına kaydediliyor
        User savedUser = userRepository.save(newUser);

        // Kullanıcı için bir Authentication nesnesi oluşturuluyor
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userDto.getEmail(), userDto.getPassword());

        // SecurityContext'e bu authentication bilgisi ekleniyor
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Kullanıcıya özel JWT token oluşturuluyor
        String jwt = jwtProvider.generateToken(authentication);

        // AuthResponse nesnesi oluşturuluyor ve geriye döndürülüyor
        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(jwt);
        authResponse.setMessage("Register Successfully!");
        authResponse.setUser(UserMapper.toDTO(savedUser));

        return authResponse;
    }

    /**
     * Kullanıcı giriş işlemlerini gerçekleştiren metod.
     * - Email ve şifre doğrulaması yapar.
     * - Kullanıcı başarılı giriş yaparsa JWT token üretir.
     * - Kullanıcının son giriş tarihini günceller.
     */
    @Override
    public AuthResponse logIn(UserDto userDto) throws UserException {
        String email = userDto.getEmail();
        String password = userDto.getPassword();

        // Email ve şifre doğrulanıyor
        Authentication authentication = authenticate(email, password);

        // Doğrulama başarılıysa SecurityContext'e ekleniyor
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Kullanıcının rollerine erişiliyor
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String role = authorities.iterator().next().getAuthority();

        // JWT token üretiliyor
        String jwt = jwtProvider.generateToken(authentication);

        // Kullanıcının son giriş tarihi güncelleniyor
        User user = userRepository.findByEmail(email);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // AuthResponse nesnesi oluşturuluyor ve geriye döndürülüyor
        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(jwt);
        authResponse.setMessage("Login Successfully!");
        authResponse.setUser(UserMapper.toDTO(user));

        return authResponse;
    }

    /**
     * Kullanıcı kimlik doğrulamasını yapan yardımcı metod.
     * - Email adresi veritabanında kayıtlı mı kontrol eder.
     * - Şifre doğru mu diye doğrular.
     * - Başarılıysa Authentication nesnesi döner.
     */
    private Authentication authenticate(String email, String password) throws UserException {
        // Kullanıcı bilgileri veritabanından alınıyor
        UserDetails userDetails = customUserImpl.loadUserByUsername(email);

        // Kullanıcı bulunamazsa hata fırlatılır
        if (userDetails == null) {
            throw new UserException("Email Id doesn't exist " + email);
        }

        // Girilen şifre ile veritabanındaki hashlenmiş şifre karşılaştırılır
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new UserException("Password doesn't match");
        }

        // Başarılı giriş sonrası Authentication nesnesi döndürülür
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }
}

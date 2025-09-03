package com.akcadag.configuration;

import java.util.Arrays;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;

@Configuration
public class SecurityConfig {

    //  Spring Security yapılandırmasını yapan metod
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Oturum oluşturulmaz, her istek bağımsızdır (JWT için gerekli)
                .sessionManagement(management -> management
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Endpoint bazlı erişim yetkilendirmesi
                .authorizeHttpRequests(Authorize -> Authorize
                        .requestMatchers("/api/**").authenticated()       // /api/** -> kimlik doğrulaması gerekir
                        .requestMatchers("/api/super-admin/**")           // /api/super-admin/** -> sadece ADMIN rolü erişebilir
                        .hasRole("ADMIN")
                        .anyRequest().permitAll()                         // Diğer tüm istekler serbesttir
                )
                // JWT doğrulama filtresi eklenir
                .addFilterBefore(new JwtValidator(), BasicAuthenticationFilter.class)

                // CSRF koruması kapatılıyor (JWT ile çalışırken gerekli)
                .csrf(AbstractHttpConfigurer::disable)

                // CORS yapılandırması ekleniyor
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Yapılandırmayı tamamla
                .build();
    }

    // ✅ Şifreleri güvenli şekilde hashlemek için kullanılan bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // BCrypt ile şifreleri geri döndürülemez şekilde hashler
    }

    // ✅ CORS yapılandırmasını döndüren metod
    private CorsConfigurationSource corsConfigurationSource() {
        return new CorsConfigurationSource() {
            @Override
            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                CorsConfiguration cfg = new CorsConfiguration();

                // Sadece belirli frontend adreslerinden erişime izin ver
                cfg.setAllowedOrigins(
                        Arrays.asList(
                                "http://localhost:5000",
                                "http://localhost:3000"
                        )
                );

                // Tüm HTTP metodlarına (GET, POST, PUT, DELETE vb.) izin ver
                cfg.setAllowedMethods(Collections.singletonList("*"));

                // Kimlik doğrulama bilgileri (cookie, token vb.) gönderilebilir
                cfg.setAllowCredentials(true);

                // Tüm header'lara izin ver
                cfg.setAllowedHeaders(Collections.singletonList("*"));

                // Authorization header'ının frontend tarafından okunmasına izin ver
                cfg.setExposedHeaders(Arrays.asList("Authorization"));

                // Tarayıcı bu ayarları 1 saat boyunca cache'ler
                cfg.setMaxAge(3600L);

                return cfg;
            }
        };
    }
}

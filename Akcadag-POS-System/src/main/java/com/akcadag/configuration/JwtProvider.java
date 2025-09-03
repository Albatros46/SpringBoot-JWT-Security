package com.akcadag.configuration;

import io.jsonwebtoken.Claims;                 // JWT içindeki payload verilerini almak için kullanılır
import io.jsonwebtoken.Jwts;                   // JWT oluşturmak ve doğrulamak için ana sınıf
import io.jsonwebtoken.security.Keys;          // Gizli anahtar (Secret Key) oluşturmak için kullanılır
import org.springframework.security.core.Authentication;  // Kullanıcı doğrulama bilgilerini temsil eder
import org.springframework.security.core.GrantedAuthority; // Kullanıcının rollerini temsil eder
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey; // JWT imzalama için kullanılan gizli anahtar türü
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Service
public class JwtProvider {

    //  JWT imzalamak için kullanılan SecretKey.
    // JwtConstant.JWT_SECRET → gizli anahtar değerini içerir (örneğin 32 karakterlik bir string)
    static SecretKey key = Keys.hmacShaKeyFor(JwtConstant.JWT_SECRET.getBytes());

    /**
     *  JWT Token oluşturmak için kullanılan metod.
     * @param authentication → Spring Security'nin doğruladığı kullanıcı bilgileri
     * @return Kullanıcıya özel JWT Token döndürür
     */
    public String generateToken(Authentication authentication) {

        // Kullanıcının rollerini (authorities) alıyoruz
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // Roller tek bir string halinde virgülle ayrılarak hazırlanıyor
        String roles = populateAuthorities(authorities);

        // JWT oluşturuluyor
        return Jwts.builder()
                .issuedAt(new Date()) // Token'ın oluşturulma zamanı
                .expiration(new Date(new Date().getTime() + 86400000)) // Token geçerlilik süresi → 1 gün (24 saat)
                .claim("email", authentication.getName()) // JWT içerisine kullanıcının email bilgisini ekliyoruz
                .claim("authorities", roles) // JWT içerisine kullanıcının rollerini ekliyoruz
                .signWith(key) // Token'ı SecretKey ile imzalıyoruz
                .compact(); // Token'ı oluştur ve String olarak döndür
    }

    /**
     *  JWT Token içinden kullanıcı email bilgisini almak için kullanılan metod.
     * @param jwt → Kullanıcıdan gelen JWT token
     * @return Token içindeki email adresini döndürür
     */
    public String getEmailFromToken(String jwt) {

        // "Bearer " prefix'ini kaldırıyoruz (Authorization header'dan gelen token'da olur)
        jwt = jwt.substring(7);

        // JWT içindeki verileri (claims) almak için parsing işlemi
        Claims claims = Jwts.parser()
                .verifyWith(key) // Token doğrulaması için SecretKey kullanıyoruz
                .build()
                .parseSignedClaims(jwt) // Token'ı parse ediyoruz
                .getPayload(); // Payload kısmını alıyoruz

        // "email" bilgisini claim'lerden çekiyoruz
        return String.valueOf(claims.get("email"));
    }

    /**
     *  Kullanıcının rollerini (authorities) virgülle ayrılmış bir string haline dönüştürür.
     * Örneğin: ["ROLE_ADMIN", "ROLE_USER"] → "ROLE_ADMIN,ROLE_USER"
     */
    private String populateAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Set<String> auths = new HashSet<>();

        // Kullanıcının sahip olduğu tüm rolleri Set içerisine ekliyoruz
        for (GrantedAuthority authority : authorities) {
            auths.add(authority.getAuthority());
        }

        // Roller virgülle ayrılarak string'e dönüştürülüyor
        return String.join(",", auths);
    }
}

package com.akcadag.configuration;

import io.jsonwebtoken.Claims;                       // JWT içindeki payload verilerini almak için kullanılır
import io.jsonwebtoken.Jwts;                         // JWT oluşturmak ve doğrulamak için ana sınıf
import io.jsonwebtoken.security.Keys;                // Gizli anahtar (Secret Key) oluşturmak için kullanılır
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException; // Hatalı kimlik doğrulama hatası
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Spring Security Authentication objesi
import org.springframework.security.core.Authentication; // Doğrulanan kullanıcıyı temsil eder
import org.springframework.security.core.GrantedAuthority; // Kullanıcının rollerini temsil eder
import org.springframework.security.core.authority.AuthorityUtils; // Roller string → GrantedAuthority listesine çevrilir
import org.springframework.security.core.context.SecurityContextHolder; // Spring Security Context’e erişim sağlar
import org.springframework.web.filter.OncePerRequestFilter; // Her istekte bir kez çalıştırılan özel filtre

import javax.crypto.SecretKey; // JWT doğrulamak için kullanılan SecretKey
import java.io.IOException;
import java.util.List;

/**
 *  JwtValidator
 * Bu sınıf Spring Security'nin "custom filter"ıdır.
 * Her HTTP isteğinde gelen JWT'yi doğrular, geçerliyse Authentication oluşturur.
 */
public class JwtValidator extends OncePerRequestFilter {

    /**
     *  Her HTTP isteğinde bir kez çalışır.
     * @param request → Gelen HTTP isteği
     * @param response → Sunucudan dönecek cevap
     * @param filterChain → Diğer filtreleri çalıştırmaya devam etmek için kullanılır
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Authorization header'dan JWT alınır
        String jwt = request.getHeader(JwtConstant.JWT_HEADER);

        // Beklenen format → "Bearer <token>"
        if (jwt != null) {
            jwt = jwt.substring(7); // "Bearer " kısmını atlıyoruz

            try {
                //  Token'ı doğrulamak için SecretKey oluşturuluyor
                SecretKey key = Keys.hmacShaKeyFor(JwtConstant.JWT_SECRET.getBytes());

                //  Token çözülüyor ve claim'ler alınıyor
                Claims claims = Jwts.parser()
                        .verifyWith(key)    // Token imzasını SecretKey ile doğrula
                        .build()
                        .parseSignedClaims(jwt) // JWT parse ediliyor
                        .getPayload(); // Payload kısmını alıyoruz

                //  Token içindeki kullanıcı email bilgisi alınıyor
                String email = String.valueOf(claims.get("email"));

                //  Token içindeki kullanıcı rolleri (authorities) alınıyor
                String authorities = String.valueOf(claims.get("authorities"));

                //  Roller, GrantedAuthority listesine çevriliyor
                List<GrantedAuthority> auths =
                        AuthorityUtils.commaSeparatedStringToAuthorityList(authorities);

                //  Authentication nesnesi oluşturuluyor
                Authentication auth =
                        new UsernamePasswordAuthenticationToken(email, null, auths);

                //  Authentication nesnesi SecurityContext’e ekleniyor
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (Exception e) {
                // Token geçersiz, süresi dolmuş veya hatalıysa hata fırlatılır
                throw new BadCredentialsException("Invalid JWT...");
            }
        }

        //  Filtre zincirine devam et → diğer filtreleri çalıştır
        filterChain.doFilter(request, response);
    }
}

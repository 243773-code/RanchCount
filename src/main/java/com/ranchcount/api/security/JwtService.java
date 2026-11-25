package com.ranchcount.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(
            @Value("${app.security.jwt.secret:}") String secret,
            @Value("${app.security.jwt.expiration-ms:86400000}") long expirationMs) {

        if (secret == null || secret.isBlank()) {
            log.warn("JWT_SECRET no definido en variables de entorno ni en .env; generando llave criptografica efimera HS256 para esta sesion.");
            this.signingKey = Jwts.SIG.HS256.key().build();
        } else if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET debe tener al menos 32 bytes (256 bits) para garantizar seguridad HMAC-SHA256."
            );
        } else {
            this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }
        this.expirationMs = expirationMs;
    }

    public String generateToken(SecurityUser securityUser) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(securityUser.getUsername())
                .claim("uid", securityUser.getId())
                .claim("rol", securityUser.getUsuario().getRol().name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equalsIgnoreCase(userDetails.getUsername())
                && userDetails.isEnabled()
                && !isTokenExpired(token);
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    private boolean isTokenExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

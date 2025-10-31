package org.example.security;

import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.*;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;

import java.security.Key;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
@Component
public class JwtUtil {

    private final Key signingKey;
    private final long expirationMillis;

    // Pro jednoduchou revokaci tokenů (v paměti)
    private final Set<String> revokedTokens = ConcurrentHashMap.newKeySet();

    public JwtUtil(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration-ms:3600000}") long expirationMillis) { // default 1h
        if (secretKey == null || secretKey.isBlank() || secretKey.length() < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 characters");
        }
        this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationMillis;
    }

    // -------- Generování tokenu --------
    public String generateToken(String username) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMillis))
                .signWith(signingKey)
                .compact();
    }

    // -------- Parsování a validace --------
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = parseToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            throw new JwtAuthenticationException("Invalid JWT token", e);
        }
    }

    public boolean validateToken(String token, String username) {
        String tokenUsername = extractUsername(token);
        return tokenUsername.equals(username) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    // -------- Custom exception --------
    public static class JwtAuthenticationException extends RuntimeException {
        public JwtAuthenticationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}

package org.example.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {
    private final Key signingKey;
    private final long accessTokenExpirationMillis;

    private final long refreshTokenExpirationMillis;


    public JwtService(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-expiration-ms:900000}") long accessTokenExpirationMillis, // default 15 min
            @Value("${jwt.refresh-token-expiration-ms:604800000}") long refreshTokenExpirationMillis // default 7 days
    ) {
        if (secretKey == null || secretKey.isBlank() || secretKey.length() < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 characters");
        }
        this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMillis = accessTokenExpirationMillis;
        this.refreshTokenExpirationMillis = refreshTokenExpirationMillis;
    }

    // -------- Generování tokenu --------
    public String generateAccessToken(String username) {
       return generateToken(username, accessTokenExpirationMillis);
    }

    public String generateRefreshToken(String username) {
        return generateToken(username, refreshTokenExpirationMillis);
    }
    private String generateToken(String username, long expirationMillis) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMillis))
                .signWith(signingKey)
                .compact();
    }

    // -------- Parsování a validace --------
    public boolean validateToken(String Token, String username){
        final String tokenUsername = extractUsername(Token);
        return (tokenUsername.equals(username) && !isTokenExpired(Token));
    }

    private boolean isTokenExpired(String token){
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    // -------- Extrakce údajů z tokenu --------
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaim(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaim(String token) {
        try {
            return Jwts.parser()
                    .verifyWith((SecretKey) signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    // -------- Blacklist pro refresh tokeny --------



}

package krematos.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import krematos.exception.token.InvalidTokenException;
import krematos.repository.BlacklistedTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import krematos.model.BlacklistedToken;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.time.Clock;


@Service
public class JwtService {
    private final Key signingKey;
    private final long accessTokenExpirationMillis;

    private final long refreshTokenExpirationMillis;

    private final BlacklistedTokenRepository blacklistedTokenRepository;

    private final Clock clock;




    public JwtService(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-expiration-ms:900000}") long accessTokenExpirationMillis, // default 15 min
            @Value("${jwt.refresh-token-expiration-ms:604800000}") long refreshTokenExpirationMillis, // default 7 days
            BlacklistedTokenRepository blacklistedTokenRepository,
                       Clock clock
    ) {
        if (secretKey == null || secretKey.isBlank() || secretKey.length() < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 characters");
        }
        this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMillis = accessTokenExpirationMillis;
        this.refreshTokenExpirationMillis = refreshTokenExpirationMillis;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
        this.clock = clock;
    }

    // -------- Generování tokenu --------
    public String generateAccessToken(String username) {
       return generateToken(username, accessTokenExpirationMillis);
    }

    public String generateRefreshToken(String username) {
        return generateToken(username, refreshTokenExpirationMillis);
    }
    // Společná metoda pro generování tokenu
    private String generateToken(String username, long expirationMillis) {
        Date now = Date.from(Instant.now(clock));
        Date expiryDate = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith((SecretKey) signingKey)
                .compact();
    }

    // -------- Parsování a validace --------
    public boolean validateToken(String token, String username){
        final String tokenUsername = extractUsername(token);
        return (tokenUsername.equals(username) && !isTokenExpired(token));
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
    // Metoda pro extrakci všech claimů z tokenu
    private Claims extractAllClaim(String token) {
        try {
            return Jwts.parser()
                    .verifyWith((SecretKey) signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (IllegalArgumentException e) {
            throw new InvalidTokenException("Token JWT nesmí být prázdný nebo null");
        } catch (JwtException e) {
            throw new InvalidTokenException("Neplatný token JWT");
        }
    }



    // -------- Blacklist pro refresh tokeny --------
    // 3. Metoda pro Blacklist (volá se při Logoutu)
    public void blacklistToken(String token) {
        Date expirationDate = extractClaim(token, Claims::getExpiration);
        Instant expiryDate = expirationDate.toInstant();
        BlacklistedToken blacklistedToken = new BlacklistedToken(token, expiryDate);
        blacklistedTokenRepository.save(blacklistedToken);
    }
    // 4. Metoda pro kontrolu (volá se při každém Requestu)
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokenRepository.existsByToken(token);
    }


    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaim(token);
        Object rolesObject = claims.get("roles");
        if (rolesObject instanceof List<?>) {
            return ((List<?>) rolesObject).stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }
        return List.of();
    }
}

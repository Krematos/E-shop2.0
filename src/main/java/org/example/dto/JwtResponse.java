package org.example.dto;

import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;
import java.util.Set;

public record JwtResponse(String accessToken,
                          String tokenType,
                          String username,
                          Set<String> roles) {
    public JwtResponse(String accessToken, String username, Collection<? extends GrantedAuthority> authorities) {
    this(
            accessToken,
            "Bearer", // Defaultní typ tokenu podle standardu
            username,
            authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(java.util.stream.Collectors.toSet())
    );
    }
}

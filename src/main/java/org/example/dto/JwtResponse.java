package org.example.dto;

import org.springframework.security.core.GrantedAuthority;
import java.util.List;
import java.util.Collection;

public record JwtResponse(String accessToken,
                          String tokenType,
                          String username,
                          List<String> roles) {
    public JwtResponse(String accessToken, String username, Collection<? extends GrantedAuthority> authorities) {
    this(
            accessToken,
            "Bearer", // Defaultní typ tokenu podle standardu
            username,
            authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList()
    );
    }
}

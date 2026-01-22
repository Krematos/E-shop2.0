package org.example.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.Collections;
import java.util.stream.Collectors;

// Import statických permissions pro lepší čitelnost
import static org.example.model.enums.Permission.*;


@RequiredArgsConstructor
public enum Role {
    ROLE_USER(Collections.emptySet()),
    ROLE_ADMIN(Set.of(
            ADMIN_READ,
            ADMIN_UPDATE,
            ADMIN_CREATE,
            ADMIN_DELETE,
            USER_READ,
            USER_UPDATE
    )
    ),
    ROLE_MANAGER(Set.of(
            USER_READ,
            USER_UPDATE
    )
    );



    @Getter
    private final Set<Permission> permissions;

    // Metoda, která převede Enumy na objekty, kterým rozumí Spring Security
    public List<SimpleGrantedAuthority> getAuthorities() {
        var authorities = getPermissions()
                .stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toList());

        // Přidá i samotnou roli (aby fungovalo i staré hasRole('ADMIN'))
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));

        return authorities;
    }
}

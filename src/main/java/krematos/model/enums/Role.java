package krematos.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.Collections;
import java.util.stream.Collectors;

// Import statických permissions pro lepší čitelnost


@RequiredArgsConstructor
public enum Role {
    ROLE_USER(Collections.emptySet()),
    ROLE_ADMIN(Set.of(
            Permission.ADMIN_READ,
            Permission.ADMIN_UPDATE,
            Permission.ADMIN_CREATE,
            Permission.ADMIN_DELETE,
            Permission.USER_READ,
            Permission.USER_UPDATE
    )
    ),
    ROLE_MANAGER(Set.of(
            Permission.USER_READ,
            Permission.USER_UPDATE
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

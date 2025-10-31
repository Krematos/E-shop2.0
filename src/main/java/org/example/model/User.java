package org.example.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.validation.constraints.Size;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users", schema = "public")
@Builder
@Getter
@Setter
public class User {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    @Size(min = 3, max = 30)
    private String username;
    @Column(nullable = false)
    @Size(min = 6, max = 100)
    private String password;

    @Column(nullable = false, unique = true)
    @Size(min = 5, max = 50)
    private String email;

    @Column(name = "roles")
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();



    // Vrací Spring Security autority
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toSet());
    }

    // Přidá jednu roli, nepřepisuje existující
    public void addRole(Role role) {
        if (role != null) roles.add(role);
    }




    public String getRole() {
        return roles.stream()
                .findFirst()
                .map(Enum::name)
                .orElse(Role.ROLE_USER.name()); // Default role if none set
    }

    public void setRole(String role) {
        if (role != null && !role.isEmpty()) {
            String normalizedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
            this.roles = Set.of(Role.valueOf(role));
        } else {
            this.roles = Set.of(Role.ROLE_USER); // Default role
        }
    }







    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return username.equals(user.username) && email.equals(user.email);
    }

    @Override
    public int hashCode() {
        return 31 * username.hashCode() + email.hashCode();
    }

    public enum Role {
        ROLE_USER,
        ROLE_ADMIN
    }
}

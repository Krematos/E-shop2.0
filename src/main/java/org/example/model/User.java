package org.example.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.validation.constraints.Size;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", schema = "public")
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

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();

    // Getters and Setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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


    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles != null ? roles : new HashSet<>();
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

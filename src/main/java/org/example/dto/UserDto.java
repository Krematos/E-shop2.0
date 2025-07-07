package org.example.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.model.User;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private static final Set<String> VALID_ROLES = new HashSet<>();
    private Long id;

    @NotBlank(message = "Zadejte uživatelské jméno")
    @Size(min = 3, max = 50, message = "Uživatelské jméno musí mít od 3 do 50 znaků")
    private String username;

    @NotBlank(message = "Zadejte email")
    @Email(message = "Email musí být platný")
    private String email;

    @NotBlank(message = "Zadejte heslo")
    @Size(min = 6, max = 100, message = "Heslo musí mít od 6 do 100 znaků")
    private String password;

    @Pattern(regexp = "^(ADMIN|USER)$", message = "Role musí být buď ADMIN nebo USER")
    private String role;

    public void setId(Long id) {
        this.id = id;
    }
    public Long getId() {
        return id;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public Set<String> getRoles() {
        Set<String> roles = new HashSet<>();
        roles.add(role);
        return roles;
    }

    public void setRoles(String role) {
        if (VALID_ROLES.contains(role)) {
            this.role = role;
        } else {
            throw new IllegalArgumentException("Neplatná role: " + role);
        }
    }

}

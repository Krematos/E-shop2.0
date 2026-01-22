package org.example.dto.user;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegistrationRequest(
        @NotBlank(message = "Uživatelské jméno nesmí být prázdné")
        @Size(min = 3, max = 50, message = "Uživatelské jméno musí mít od 3 do 50 znaků")
        String username,

        @NotBlank(message = "Email nesmí být prázdný")
        @Size(min = 5, max = 100, message = "Email musí mít od 5 do 100 znaků")
        String email,

        @NotBlank(message = "Heslo nesmí být prázdné")
        @Size(min = 6, max = 100, message = "Heslo musí mít od 6 do 100 znaků")
        String password
) {
}

package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Uživatelské jméno nesmí být prázdné")
        @Size(min = 3, max = 50)
        String username,

        @NotBlank(message = "Heslo nesmí být prázdné")
        @Size(min = 6, max = 100)
        String password
) {
}

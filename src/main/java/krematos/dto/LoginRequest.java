package krematos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Uživatelské jméno nesmí být prázdné")
        @Size(min = 3, max = 50, message = "Uživatelské jméno musí mít mezi 3 až 50 znaky")
        String username,

        @NotBlank(message = "Heslo nesmí být prázdné")
        @Size(min = 6, max = 100, message = "Heslo musí mít alespoň 8 znaků")
        String password
) {
}

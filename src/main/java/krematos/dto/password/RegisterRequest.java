package krematos.dto.password;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest (
        @NotBlank
        @Size(min = 3, max = 30, message = "Jméno musí mít 3-30 znaků")
        String username,
        @NotBlank (message = "Email nesmí být prázdný")
        @Email (message = "Neplatná emailová adresa")
        @Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "Email musí obsahovat platnou koncovku (např. .com, .cz)")
        String email,
        @NotBlank @Size(min= 8)
        String password) {
}

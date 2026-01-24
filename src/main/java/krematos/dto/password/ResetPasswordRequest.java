package krematos.dto.password;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank
        String token,
        @NotBlank (message = "Heslo nesmí být prázdné")
        @Size(min = 8, message = "Heslo musí mít alespoň 8 znaků")
        String newPassword) {
}

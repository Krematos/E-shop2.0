package krematos.dto.password;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @NotBlank(message = "Email nesmí být prázdný")
        @Email(message = "Neplatná emailová adresa")
        String email) {
}

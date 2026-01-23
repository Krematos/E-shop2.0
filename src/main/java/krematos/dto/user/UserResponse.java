package krematos.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserResponse(
Long id,

@NotBlank(message = "Zadejte uživatelské jméno")
@Size(min = 3, max = 50, message = "Uživatelské jméno musí mít od 3 do 50 znaků")
String username,

@NotBlank(message = "Zadejte email")
@Email(message = "Email musí být platný")
String email,

@NotBlank(message = "Zadejte heslo")
@Size(min = 8, max = 100, message = "Heslo musí mít od 8 do 100 znaků")
String password,

@Pattern(regexp = "^(ADMIN|USER)$", message = "Role musí být buď ADMIN nebo USER")
String role) {

}

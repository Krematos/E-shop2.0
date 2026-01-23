package krematos.dto.user;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateResponse(
        @NotBlank(message = "Křestní jméno nesmí být prázdné")
        @Size(min = 2, max = 50, message = "Křestní jméno musí mít od 2 do 50 znaků")
        String firstName,

        @NotBlank(message = "Příjmení nesmí být prázdné")
        @Size(min = 2, max = 50, message = "Příjmení musí mít od 2 do 50 znaků")
        String lastName,

        @NotBlank(message = "Email nesmí být prázdný")
        @Size(min = 5, max = 100, message = "Email musí mít od 5 do 100 znaků")
        String email       

) {
}

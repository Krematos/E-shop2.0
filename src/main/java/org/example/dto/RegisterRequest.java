package org.example.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest (
        @NotBlank
        @Size(min = 3, max = 30, message = "Jméno musí mít 3-30 znaků")
        String username,
        @NotBlank @Email
        String email,
        @NotBlank @Size(min= 8)
        String password) {
}

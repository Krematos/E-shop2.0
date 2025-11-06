package org.example.dto;


import lombok.*;

@Data@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterDto {
    private String username;
    private String password;
    private String email;

}

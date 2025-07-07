package org.example.controller;


import jakarta.validation.Valid; // Importy pro validaci
import org.example.dto.LoginDto; // DTO pro přihlášení uživatele
import org.example.dto.RegisterDto; // DTO pro registraci uživatele
import org.example.dto.UserDto;
import org.example.model.User;  // Model uživatele
import org.example.service.UserService; // Služba pro správu uživatelů
import org.springframework.http.HttpStatus;  // Importy pro HTTP statusy
import org.springframework.http.ResponseEntity; // Importy pro odpovědi REST API
import org.springframework.security.authentication.AuthenticationManager; // Importy pro správu autentizace
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Importy pro autentizaci uživatele pomocí jména a hesla
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder; // Importy pro správu kontextu bezpečnosti
import org.springframework.web.bind.annotation.PostMapping; // Importy pro HTTP POST metody
import org.springframework.web.bind.annotation.RequestBody; // Importy pro tělo požadavku
import org.springframework.web.bind.annotation.RequestMapping; // Importy pro mapování cest
import org.springframework.web.bind.annotation.RestController; // Importy pro REST kontroler

@RestController // Označuje, že tato třída je REST kontroler
@RequestMapping("/api/auth") // Definuje základní cestu pro všechny metody v tomto kontroleru
public class AuthController {

    private final UserService userService; // Služba pro správu uživatelů
    private final AuthenticationManager authenticationManager; // Správce autentizace pro ověřování uživatelů

    // Konstruktor pro injektování závislostí
    public AuthController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    // Metoda pro registraci nového uživatele
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody RegisterDto registrationDto) {
        try{
            User newUser = new User();
            newUser.setUsername(registrationDto.getUsername());
            newUser.setPassword(registrationDto.getPassword()); // Bude zahešováno v UserService
            newUser.setEmail(registrationDto.getEmail());
            userService.registerNewUser(newUser);
            return new ResponseEntity<>("Uživatel úspěšně zaregistrován", HttpStatus.CREATED);
        }
        catch (Exception e) {
            return new ResponseEntity<>("Chyba při registraci uživatele: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }



    @PostMapping("/login")
    public ResponseEntity<String> authenticateUser(@Valid @RequestBody LoginDto loginDto) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(auth);
        return new ResponseEntity<>("Uživatel úspěšně přihlášen", HttpStatus.OK);
    }

    @PostMapping("/logout") // Metoda pro odhlášení uživatele
    public ResponseEntity<String> logoutUser() {
        SecurityContextHolder.clearContext(); // Vymaže kontext bezpečnosti, čímž se uživatel odhlásí
        return new ResponseEntity<>("Uživatel úspěšně odhlášen", HttpStatus.OK);
    }





}

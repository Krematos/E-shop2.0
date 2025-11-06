package org.example.controller;




import lombok.extern.slf4j.Slf4j;
import org.example.model.User;
import org.example.security.JwtUtil;
import org.example.service.UserService;
import org.example.service.impl.UserDetailsImpl;
import org.example.service.impl.UserDetailsServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController // Označuje, že tato třída je REST kontroler
@RequestMapping("/api/auth") // Definuje základní cestu pro všechny metody v tomto kontroleru
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsServiceImpl userDetailsService;

    public AuthController(AuthenticationManager authenticationManager,
                          UserService userService,
                          JwtUtil jwtUtil,
                          PasswordEncoder passwordEncoder,
                          UserDetailsServiceImpl userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }

    // ✅ Registrace nového uživatele
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User newUser = userService.registerNewUser(user);
            return ResponseEntity.ok(Map.of(
                    "message", "Registrace proběhla úspěšně",
                    "username", newUser.getUsername(),
                    "email", newUser.getEmail()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Přihlášení a vydání JWT tokenu
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails.getUsername());

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "username", userDetails.getUsername(),
                    "roles", userDetails.getAuthorities()
            ));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Neplatné přihlašovací údaje"));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Chyba při autentizaci"));
        }
    }

    // ✅ Ověření JWT tokenu (např. pro FE)
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String tokenHeader) {
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Chybí token"));
        }

        String token = tokenHeader.substring(7);
        try {
            String username = jwtUtil.extractUsername(token);
            boolean valid = jwtUtil.validateToken(token, username);
            return ResponseEntity.ok(Map.of(
                    "valid", valid,
                    "username", username
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Neplatný token: " + e.getMessage()));
        }
    }



}

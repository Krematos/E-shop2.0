package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.User;
import org.example.service.JwtService;
import org.example.service.email.EmailService;
import org.example.service.user.UserService;
import org.example.service.impl.UserDetailsImpl;
import org.example.service.impl.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Value;
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
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsServiceImpl userDetailsService;
    private final EmailService emailService;
    private final JwtService jwtService;
    @Value("${app.frontend.url}")
    private String frontendUrl;



    // ✅ Registrace nového uživatele
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        log.info("POST /api/auth/register - Pokus o registraci uživatele: {}", user.getUsername());
        try {
            User newUser = userService.registerNewUser(user);
            log.info("Uživatel {} byl úspěšně zaregistrován.", newUser.getUsername());
            return ResponseEntity.ok(Map.of(
                    "message", "Registrace proběhla úspěšně",
                    "username", newUser.getUsername(),
                    "email", newUser.getEmail()
            ));
        } catch (IllegalArgumentException e) {
            log.error("Chyba při registraci uživatele {}: {}", user.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        log.info("POST /api/auth/forgot-password - Požadavek na reset hesla pro email: {}", email);
        if (email == null || email.isBlank()) {
            log.warn("Požadavek na reset hesla selhal: Emailová adresa je prázdná.");
            return ResponseEntity.badRequest().body(Map.of("error", "Emailová adresa nesmí být prázdná."));
        }
        try {
            String token = userService.createPasswordResetTokenForUser(email);
            String resetUrl = frontendUrl + "/reset-password?token" + token;
            emailService.sendPasswordResetEmail(email, resetUrl);
            log.info("Odkaz pro reset hesla byl odeslán na email: {}", email);

        } catch (Exception e) {
            log.error("Chyba při zpracování požadavku na reset hesla pro email {}: {}", email, e.getMessage());
        }
        return ResponseEntity.ok(Map.of("message", "Pokud je tento e-mail registrován, instrukce byly odeslány."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");
        log.info("POST /api/auth/reset-password - Pokus o reset hesla s tokenem: {}", token);
        try {
            userService.resetPassword(token, newPassword);
            log.info("Heslo pro token {} bylo úspěšně resetováno.", token);
            return ResponseEntity.ok(Map.of("message", "Password has been reset successfully."));
        } catch (IllegalArgumentException e) {
            log.error("Chyba při resetu hesla s tokenem {}: {}", token, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Přihlášení a vydání JWT tokenu
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        log.info("POST /api/auth/login - Pokus o přihlášení uživatele: {}", username);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String token = jwtService.generateAccessToken(userDetails.getUsername());

            log.info("Uživatel {} byl úspěšně autentizován.", username);
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "username", userDetails.getUsername(),
                    "roles", userDetails.getAuthorities()
            ));
        } catch (BadCredentialsException e) {
            log.warn("Neúspěšný pokus o přihlášení pro uživatele {}: Neplatné přihlašovací údaje", username);
            return ResponseEntity.status(401).body(Map.of("error", "Neplatné přihlašovací údaje"));
        } catch (AuthenticationException e) {
            log.error("Chyba při autentizaci pro uživatele {}: {}", username, e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Chyba při autentizaci"));
        }
    }

    // ✅ Ověření JWT tokenu (např. pro FE)
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String tokenHeader) {
        log.info("GET /api/auth/validate - Pokus o validaci tokenu");
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            log.warn("Validace tokenu selhala: Chybí 'Bearer ' prefix v hlavičce.");
            return ResponseEntity.badRequest().body(Map.of("error", "Chybí token"));
        }

        String token = tokenHeader.substring(7);
        try {
            String username = jwtService.extractUsername(token);
            boolean valid = jwtService.validateToken(token, username);
            log.info("Validace tokenu pro uživatele {}: {}", username, valid ? "úspěšná" : "neúspěšná");
            return ResponseEntity.ok(Map.of(
                    "valid", valid,
                    "username", username
            ));
        } catch (Exception e) {
            log.error("Chyba při validaci tokenu: {}", e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "Neplatný token: " + e.getMessage()));
        }
    }
    // ✅ Odhlášení uživatele (blacklist tokenu)
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request){
            log.info("POST /api/auth/logout - Uživatelský odhlášení");
            // získání tokenu z hlavičky Authorization
        String authHeader = request.getHeader("Authorization");
        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            jwtService.blacklistToken(token);
            log.info("Token byl úspěšně přidán na černou listinu.");
        } else {
            log.warn("Odhlášení selhalo: Chybí 'Bearer ' prefix v hlavičce.");
            return ResponseEntity.badRequest().body(Map.of("error", "Chybí token"));
        }
        return ResponseEntity.ok(Map.of("message", "Úspěšně odhlášeno"));
    }



}

package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.JwtResponse;
import org.example.dto.LoginRequest;
import org.example.model.User;
import org.example.service.JwtService;
import org.example.service.user.UserService;
import org.example.service.impl.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController // Označuje, že tato třída je REST kontroler
@RequestMapping("/api/auth") // Definuje základní cestu pro všechny metody v tomto kontroleru
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * ✅ Registrace nového uživatele
     * @param user
     * @return
     */

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
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

    /** Uživatel resetuje heslo pomocí tokenu
     *
     *  Map obsahující token a nové heslo.
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("POST /api/auth/login - Pokus o přihlášení uživatele: {}", loginRequest.username());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.username(),
                            loginRequest.password()
                    )
            );
            // 2. Nastavení kontextu pro aktuální vlákno
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 3. Získání detailů uživatele (UserDetails)
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // 4. Generování JWT tokenu
            String jwt = jwtService.generateAccessToken(userDetails.getUsername());

            // 5. Návrat odpovědi s tokenem
            return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getUsername(), userDetails.getAuthorities()));
        } catch (BadCredentialsException e) {
            log.warn("Neúspěšný pokus o přihlášení pro uživatele {}: Neplatné přihlašovací údaje", loginRequest.username());
            return ResponseEntity.status(401).body(Map.of("error", "Neplatné přihlašovací údaje"));
        } catch (AuthenticationException e) {
            log.error("Chyba při autentizaci pro uživatele {}: {}", loginRequest.username(), e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Chyba při autentizaci"));
        }
    }

    /**
     * ✅ Ověření JWT tokenu (např. pro FE)
     * @param tokenHeader
     * @return
     */
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

    /**
     * ✅ Odhlášení uživatele
     * @param request
     * @return
     */
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

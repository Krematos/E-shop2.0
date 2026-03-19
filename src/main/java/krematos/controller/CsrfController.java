package krematos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/api/csrf")
@RestController
@Tag(name = "CSRF Token", description = "Endpoint pro získání CSRF tokenu pro zabezpečení formulářů a AJAX požadavků")
public class CsrfController {

    @Operation(summary = "Získání CSRF tokenu", description = "Vrátí aktuální CSRF token, který může být použit pro zabezpečení formulářů a AJAX požadavků. " +
            "Tento endpoint je veřejný a nevyžaduje autentizaci.")
    @GetMapping("/token")
    public CsrfToken getCsrfToken(CsrfToken csfrToken) {
        return csfrToken;
    }
}

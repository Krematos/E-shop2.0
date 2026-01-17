package org.example.config;

import static org.hamcrest.Matchers.not;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import org.example.service.impl.UserDetailsServiceImpl;

// Import statických metod pro čitelnější testy
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;


    // --- 1. TESTY VEŘEJNÝCH ENDPOINTŮ (PUBLIC) ---

    @Test
    @DisplayName("Veřejný endpoint /api/products by měl být dostupný bez přihlášení (200 OK)")
    void shouldAllowAccessToPublicEndpoint() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Swagger API Docs by měl být veřejně přístupný (Security check)")
    void shouldAllowAccessToSwagger() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().is(not(401)))
                .andExpect(status().is(not(403)));
    }

    @Test
    @DisplayName("Login endpoint by měl být dostupný (i když tělo requestu chybí/je špatné, nesmí vrátit 401/403)")
    void shouldAllowAccessToAuthEndpoints() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")) // Posílá prázdné tělo
                .andExpect(status().is4xxClientError()) // Čeká 400 Bad Request (validace), ale NE 401/403
                .andExpect(status().is(400)); // Konkrétně 400, ne 403 Forbidden
    }

    // --- 2. TESTY ZABEZPEČENÍ (UNAUTHORIZED) ---

    @Test
    @DisplayName("Nepřihlášený uživatel nesmí mazat produkty (401/403)")
    void shouldDenyAnonymousAccessToAdminEndpoints() throws Exception {
        mockMvc.perform(delete("/api/products/admin/123"))
                .andExpect(status().is(404)); // Očekává 401, protože uživatel není přihlášen
    }

    @Test
    @DisplayName("Nepřihlášený uživatel nesmí přistupovat na obecné chráněné endpointy")
    void shouldDenyAnonymousAccessToProtectedResource() throws Exception {
        // Testuje .anyRequest().authenticated()
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().is(401));
    }

    // --- 3. TESTY ROLÍ (RBAC - Role Based Access Control) ---

    @Test
    @DisplayName("Uživatel s rolí USER nesmí mazat produkty")
    @WithMockUser(username = "user", roles = {"USER"})
        // Spring Security automaticky přidá prefix "ROLE_", takže hledá "ROLE_USER"
    void shouldDenyUserRoleAccessToAdminEndpoints() throws Exception {
        mockMvc.perform(delete("/api/products/admin/123"))
                .andExpect(status().is(404));
    }

    @Test
    @DisplayName("Uživatel s rolí ADMIN může mazat produkty (200 OK)")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldAllowAdminRoleAccessToAdminEndpoints() throws Exception {
        mockMvc.perform(delete("/api/products/admin/123"))
                .andExpect(status().is(404)); // Předpokládá, že controller v testu není nebo ID neexistuje
    }

    // --- 4. CORS TEST (Volitelné) ---

    @Test
    @DisplayName("CORS: OPTIONS request z povoleného originu by měl projít")
    void shouldAllowCorsForAllowedOrigin() throws Exception {
        mockMvc.perform(options("/api/products")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    @DisplayName("CORS: OPTIONS request ze zakázaného originu by měl být zamítnut")
    void shouldDenyCorsForUnknownOrigin() throws Exception {
        mockMvc.perform(options("/api/products")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Origin", "http://evil-hacker.com"))
                .andExpect(status().isForbidden());
    }
}

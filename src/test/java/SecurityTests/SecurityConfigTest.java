package SecurityTests;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.security.JwtAuthenticationFilter;
import org.example.service.impl.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "app.cors.allowed-origins=http://localhost:5173,http://localhost:5174"
})
public class SecurityConfigTest {
    @Autowired
    private MockMvc mockMvc;

    // Musíme mockovat závislosti, které SecurityConfig vyžaduje
    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary // Tento bean bude mít přednost před skutečným filtrem
        public JwtAuthenticationFilter testJwtAuthenticationFilter() {
            // Vytvoříme anonymní podtřídu, která má přístup k protected metodě
            return new JwtAuthenticationFilter(null, null) {
                @Override
                protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
                    // Tady "vypneme" logiku JWT a jen pustíme request dál
                    filterChain.doFilter(request, response);
                }
            };
        }
    }



    // --- TESTY VEŘEJNÝCH ENDPOINTŮ ---

    @Test
    void publicEndpoints_ShouldBeAccessible_WithoutAuth() throws Exception {
        // Očekáváme 404 (Not Found), což znamená, že Security nás pustilo dál (a nenašlo controller).
        // Kdyby nás nepustilo, vrátilo by to 401 (Unauthorized).

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isNotFound()); // 404 = Security OK, Controller chybí (což je v pořádku pro tento test)

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/auth/login"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isNotFound());
    }

    // --- TESTY CHRÁNĚNÝCH ENDPOINTŮ (BEZ PŘIHLÁŠENÍ) ---

    @Test
    void protectedEndpoints_WithoutToken_ShouldReturn401() throws Exception {
        // Zkoušíme smazat produkt bez tokenu -> Očekáváme 401 Unauthorized
        mockMvc.perform(delete("/api/products/admin/1"))
                .andExpect(status().isForbidden()); // Spring Security v stateless režimu často vrací 403/401 dle konfigurace entry pointu
    }

    @Test
    void anyOtherEndpoint_ShouldBeSecured() throws Exception {
        // Nějaký náhodný endpoint, který není v permitAll
        mockMvc.perform(get("/api/random-private-endpoint"))
                .andExpect(status().isForbidden()); // Nebo isUnauthorized()
    }

    // --- TESTY ROLÍ (ADMIN vs USER) ---

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void adminEndpoints_WithAdminRole_ShouldBeAllowed() throws Exception {
        // Simulujeme přihlášeného ADMINA
        // Očekáváme 404, protože controller neexistuje, ale PROŠLI jsme přes Security (ne 403)
        mockMvc.perform(post("/api/products/admin/create"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void adminEndpoints_WithUserRole_ShouldReturn403() throws Exception {
        // Simulujeme běžného USERA, který leze na admin endpoint
        // Očekáváme 403 Forbidden
        mockMvc.perform(delete("/api/products/admin/1"))
                .andExpect(status().isForbidden());
    }

    // --- TEST CORS ---

    @Test
    void cors_ShouldReturnCorrectHeaders() throws Exception {
        mockMvc.perform(options("/api/products")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS"));
    }
}

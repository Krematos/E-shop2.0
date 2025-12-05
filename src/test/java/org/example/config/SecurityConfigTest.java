package org.example.config;

import org.example.service.JwtService;
import org.example.service.email.EmailService;
import org.example.service.user.UserService;
import org.example.service.impl.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserService userService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @Test
    void testPublicEndpoints_AccessAllowed() throws Exception {
        // Swagger UI - might return 404 if resource handlers are not fully loaded, but
        // definitely not 401/403
        // If 401/403, it means security blocked it.
        // If 200 or 404, it means security allowed it.
        // Let's accept 200 or 404 for these.
        // Actually, checking for !isForbidden() and !isUnauthorized() is safer.

        mockMvc.perform(get("/swagger-ui.html")).andExpect(status().is(200)); // Swagger usually 200 or 302
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().is(200)); // API docs usually 200

        // Auth endpoints
        // POST to /api/auth/login with no body -> 400 Bad Request (allowed)
        mockMvc.perform(post("/api/auth/login")).andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/auth/register")).andExpect(status().isBadRequest());

        // Public GET endpoints
        // If controller not present, 404. If present, 200.
        // We just want to ensure it's NOT 401/403.
        // But to be precise, we expect 404 because we didn't load controllers (except
        // if WebMvcTest scans them all)
        // @WebMvcTest without arguments scans all controllers.
        // So if ProductController exists, /api/products might return 200.
        // Let's assume 200 for now, or check that it's not 401/403.
        mockMvc.perform(get("/api/products")).andExpect(status().isOk());
        mockMvc.perform(get("/api/categories")).andExpect(status().isOk());
    }

    @Test
    void testProtectedEndpoints_Unauthenticated_AccessDenied() throws Exception {
        // Trying to create a product without login
        mockMvc.perform(post("/api/products/admin"))
                .andExpect(status().isForbidden()); // Spring Security default for denied is 403, or 401 if auth
                                                    // missing?
        // Usually 401 if no token provided (AuthenticationEntryPoint), 403 if token
        // provided but wrong role.
        // But if no entry point customized, it might be 403 or 401.
        // Let's check what happens. Usually 403 for anonymous accessing protected
        // resource.
    }

    @Test
    @WithMockUser(roles = "USER")
    void testAdminEndpoints_UserRole_AccessDenied() throws Exception {
        // User trying to access admin endpoint
        mockMvc.perform(post("/api/products/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAdminEndpoints_AdminRole_AccessAllowed() throws Exception {
        // Admin accessing admin endpoint
        // Expecting 400 because body is missing, but NOT 403
        mockMvc.perform(post("/api/products/admin"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/products/admin/1"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/api/products/admin/1"))
                .andExpect(status().isOk()); // DELETE might not need body, so 200 if controller works
    }

    @Test
    void testUploads_AccessAllowed() throws Exception {
        // /uploads/** is public
        // Expect 404 (resource not found) but NOT 401/403
        mockMvc.perform(get("/uploads/some-image.jpg"))
                .andExpect(status().isNotFound());
    }
}

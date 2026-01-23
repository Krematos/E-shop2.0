package krematos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import krematos.config.SecurityConfig;
import krematos.dto.LoginRequest;
import krematos.model.User;
import krematos.model.enums.Role;
import krematos.service.JwtService;
import krematos.service.email.EmailService;
import krematos.service.impl.UserDetailsImpl;
import krematos.service.impl.UserDetailsServiceImpl;
import krematos.service.user.UserService;
import krematos.dto.password.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ SecurityConfig.class })
class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private AuthenticationManager authenticationManager;

        @MockBean(name = "userService")
        private UserService userService;

        @MockBean
        private JwtService jwtService;

        @MockBean
        private PasswordEncoder passwordEncoder;

        @MockBean
        private UserDetailsServiceImpl userDetailsService;

        @MockBean
        private EmailService emailService;

        @Test
        void testRegisterUser_Success() throws Exception {
                User user = new User();
                user.setUsername("testuser");
                user.setPassword("password");
                user.setEmail("test@example.com");

                when(userService.registerNewUser(any(User.class))).thenReturn(user);

                mockMvc.perform(post("/api/auth/register")
                                .with(csrf()) // Přidá CSRF token
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(user)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Registrace proběhla úspěšně"));
        }

        @Test
        void testRegisterUser_EmailExists() throws Exception {
                User user = new User();
                user.setUsername("testuser");
                user.setPassword("password");
                user.setEmail("test@example.com");

                when(userService.registerNewUser(any(User.class)))
                                .thenThrow(new IllegalArgumentException("Email již existuje"));

                mockMvc.perform(post("/api/auth/register")
                                .with(csrf()) // Přidá CSRF token
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(user)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").value("Email již existuje"));
        }

        @Test
        @DisplayName("POST /login - Úspěšné přihlášení vrátí UserInfoResponse a JWT v cookie")
        void testAuthenticateUser_Success() throws Exception {
                // Given - validní přihlašovací údaje
                LoginRequest loginRequest = new LoginRequest("testuser", "password");

                // Příprava mocků
                User user = new User();
                user.setId(1L);
                user.setUsername("testuser");
                user.setRoles(Collections.singleton(Role.ROLE_USER));
                UserDetailsImpl userDetails = new UserDetailsImpl(user);

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());

                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                                .thenReturn(authentication);
                when(jwtService.generateAccessToken("testuser"))
                                .thenReturn("dummy-jwt-token");

                // When & Then
                mockMvc.perform(post("/api/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andDo(print())
                                .andExpect(status().isOk())
                                // Ověření JWT tokenu v Set-Cookie header
                                .andExpect(result -> {
                                        String setCookieHeader = result.getResponse().getHeader("Set-Cookie");
                                        assert setCookieHeader != null : "Set-Cookie header musí být přítomen";
                                        assert setCookieHeader.contains("accessToken=dummy-jwt-token")
                                                        : "Cookie musí obsahovat JWT token";
                                        assert setCookieHeader.contains("HttpOnly") : "Cookie musí být HttpOnly";
                                        assert setCookieHeader.contains("Path=/") : "Cookie musí mít Path=/";
                                })
                                // Ověření UserInfoResponse v body
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.username").value("testuser"))
                                .andExpect(jsonPath("$.roles").isArray())
                                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));

                // Verify interactions
                verify(authenticationManager, times(1))
                                .authenticate(any(UsernamePasswordAuthenticationToken.class));
                verify(jwtService, times(1))
                                .generateAccessToken("testuser");
        }

        @Test
        void testAuthenticateUser_BadCredentials() throws Exception {
                LoginRequest loginRequest = new LoginRequest("testuser", "wrongpassword");

                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                                .thenThrow(new BadCredentialsException("Neplatné přihlašovací údaje"));

                mockMvc.perform(post("/api/auth/login")
                                .with(csrf()) // Přidá CSRF token
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /register - Neplatný email a krátké heslo vrátí 400")
        void registerUser_ValidationErrors() throws Exception {
                // Given - neplatná data
                RegisterRequest invalidRequest = new RegisterRequest(
                                "ok", // Příliš krátké jméno (< 3)
                                "spatny-email", // Neplatný formát
                                "123" // Krátké heslo (< 8)
                );

                // When & Then
                mockMvc.perform(post("/api/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andDo(print())
                                .andExpect(status().isBadRequest()) // 400
                                // Ověří, že GlobalExceptionHandler vrátil mapu chyb
                                .andExpect(jsonPath("$.username").exists())
                                .andExpect(jsonPath("$.email").exists())
                                .andExpect(jsonPath("$.password").exists());

                // Service se nesmí zavolat, validace to stopne dřív
                verify(userService, times(0)).registerNewUser(any(User.class));
        }

        // --- BUSINESS LOGIC ERROR (Duplicity) ---

        @Test
        @DisplayName("POST /register - Duplicitní email vrátí 400 (dle Handleru)")
        void registerUser_DuplicateEmail() throws Exception {
                // Given
                RegisterRequest request = new RegisterRequest("user", "duplicate@example.com", "password123");

                // Simuluje, že service vyhodí výjimku při duplicitním emailu
                doThrow(new IllegalArgumentException("Email již existuje"))
                                .when(userService).registerNewUser(any(User.class));

                // When & Then
                mockMvc.perform(post("/api/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andDo(print())
                                .andExpect(status().isBadRequest()) // GlobalHandler mapuje IllegalArgument -> 400
                                .andExpect(jsonPath("$.error").value("Email již existuje"));
        }
}

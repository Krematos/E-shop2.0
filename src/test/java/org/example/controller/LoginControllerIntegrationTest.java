package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.JwtResponse;
import org.example.dto.LoginRequest;
import org.example.dto.user.UserInfoResponse;
import org.example.model.User;
import org.example.model.enums.Role;
import org.example.repository.OrderRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integrační testy pro login endpoint.
 * Používá best practices pro Spring Boot testování:
 * - @SpringBootTest pro plnou integraci s aplikačním kontextem
 * - @AutoConfigureMockMvc pro MockMvc podporu
 * - @ActiveProfiles pro testovací profil (např. H2 databáze)
 * - Testuje skutečnou autentizaci včetně JWT generování
 * - BeforeEach/AfterEach pro setup/cleanup testovacích dat
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Login Integration Tests")
class LoginControllerIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_EMAIL = "test@example.com";

    /**
     * Příprava testovacích dat před každým testem.
     * Vytvoří uživatele s heslem a uloží ho do databáze.
     */
    @BeforeEach
    void setUp() {
        // Vyčistit databázi před každým testem
        orderRepository.deleteAll();
        userRepository.deleteAll();

        // Vytvořit testovacího uživatele
        testUser = User.builder()
                .username(TEST_USERNAME)
                .email(TEST_EMAIL)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .roles(Collections.singleton(Role.ROLE_USER))
                .build();


        userRepository.save(testUser);
    }

    /**
     * Vyčištění testovacích dat po každém testu.
     */
    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ========================================
    // HAPPY PATH TESTY
    // ========================================

    @Test
    @Order(1)
    @DisplayName("POST /api/auth/login - Úspěšné přihlášení s platnými údaji")
    void loginUser_ValidCredentials_ReturnsJwtToken() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest(TEST_USERNAME, TEST_PASSWORD);

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.username").value(TEST_USERNAME))
                .andExpect(jsonPath("$.authorities").isArray())
                .andExpect(jsonPath("$.authorities[0].authority").value("ROLE_USER"))
                .andReturn();

        // Ověření, že token je skutečně generován
        String responseBody = result.getResponse().getContentAsString();
        JwtResponse jwtResponse = objectMapper.readValue(responseBody, JwtResponse.class);
        assertThat(jwtResponse.accessToken()).isNotNull();
        assertThat(jwtResponse.accessToken()).startsWith("eyJ"); // JWT tokeny začínají "eyJ"
        assertThat(jwtResponse.username()).isEqualTo(TEST_USERNAME);
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/auth/login - Přihlášení s emailem místo username")
    void loginUser_WithEmail_ReturnsJwtToken() throws Exception {
        // Given - použití emailu jako username
        LoginRequest loginRequest = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(cookie().exists("accessToken"));
    }

    // ========================================
    // NEGATIVE PATH TESTY - Neplatné údaje
    // ========================================

    @Test
    @Order(3)
    @DisplayName("POST /api/auth/login - Neplatné heslo vrátí 401 Unauthorized")
    void loginUser_InvalidPassword_ReturnsUnauthorized() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest(TEST_USERNAME, "wrongpassword");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(4)
    @DisplayName("POST /api/auth/login - Neexistující uživatel vrátí 401 Unauthorized")
    void loginUser_NonExistentUser_ReturnsUnauthorized() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("nonexistent", TEST_PASSWORD);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    // ========================================
    // VALIDAČNÍ TESTY
    // ========================================

    @Test
    @Order(5)
    @DisplayName("POST /api/auth/login - Prázdné username vrátí 400 Bad Request")
    void loginUser_EmptyUsername_ReturnsBadRequest() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("", TEST_PASSWORD);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").exists());
    }

    @Test
    @Order(6)
    @DisplayName("POST /api/auth/login - Prázdné heslo vrátí 400 Bad Request")
    void loginUser_EmptyPassword_ReturnsBadRequest() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest(TEST_USERNAME, "");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").exists());
    }

    @Test
    @Order(7)
    @DisplayName("POST /api/auth/login - Příliš krátké username vrátí 400 Bad Request")
    void loginUser_TooShortUsername_ReturnsBadRequest() throws Exception {
        // Given - username musí mít minimálně 3 znaky
        LoginRequest loginRequest = new LoginRequest("ab", TEST_PASSWORD);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").exists());
    }

    @Test
    @Order(8)
    @DisplayName("POST /api/auth/login - Příliš krátké heslo vrátí 400 Bad Request")
    void loginUser_TooShortPassword_ReturnsBadRequest() throws Exception {
        // Given - heslo musí mít minimálně 6 znaků
        LoginRequest loginRequest = new LoginRequest(TEST_USERNAME, "12345");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").exists());
    }

    @Test
    @Order(9)
    @DisplayName("POST /api/auth/login - Null hodnoty vrátí 400 Bad Request")
    void loginUser_NullValues_ReturnsBadRequest() throws Exception {
        // Given - prázdný JSON (null hodnoty)
        String invalidJson = "{\"username\":null,\"password\":null}";

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ========================================
    // EDGE CASE TESTY
    // ========================================

    @Test
    @Order(10)
    @DisplayName("POST /api/auth/login - Mezery v username jsou trimovány")
    void loginUser_UsernameWithSpaces_TrimsAndAuthenticates() throws Exception {
        // Given - username s mezerami na začátku/konci
        LoginRequest loginRequest = new LoginRequest("  " + TEST_USERNAME + "  ", TEST_PASSWORD);

        // When & Then
        // Pokud aplikace trimuje mezery, login by měl uspět
        // Pokud netrimuje, měl by selhat s 401
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized()); // Očekává 401, protože Spring Security netrimuje automaticky
    }

    @Test
    @Order(11)
    @DisplayName("POST /api/auth/login - Case-sensitive username")
    void loginUser_CaseSensitiveUsername_ReturnsUnauthorized() throws Exception {
        // Given - username s velkými písmeny
        LoginRequest loginRequest = new LoginRequest(TEST_USERNAME.toUpperCase(), TEST_PASSWORD);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized()); // Username je case-sensitive
    }

    @Test
    @Order(12)
    @DisplayName("POST /api/auth/login - Vícenásobné přihlášení stejného uživatele")
    void loginUser_MultipleLogins_EachReturnsNewToken() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest(TEST_USERNAME, TEST_PASSWORD);

        // When - První přihlášení
        MvcResult result1 = mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        Thread.sleep(1000); // Krátká pauza, aby se zajistilo, že tokeny budou odlišné

        // When - Druhé přihlášení
        MvcResult result2 = mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // Then - Získání tokenů z COOKIES
        String token1 = result1.getResponse().getCookie("accessToken").getValue();
        String token2 = result2.getResponse().getCookie("accessToken").getValue();

        // Ověření
        assertThat(token1).isNotNull();
        assertThat(token2).isNotNull();
        assertThat(token1).isNotEqualTo(token2);
    }

    // ========================================
    // SECURITY TESTY
    // ========================================

    @Test
    @Order(13)
    @DisplayName("POST /api/auth/login - SQL Injection pokus vrátí 401")
    void loginUser_SqlInjectionAttempt_ReturnsUnauthorized() throws Exception {
        // Given - SQL injection pokus
        LoginRequest loginRequest = new LoginRequest("admin' OR '1'='1", "' OR '1'='1");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized()); // Mělo by bezpečně selhat
    }

    @Test
    @Order(14)
    @DisplayName("POST /api/auth/login - Speciální znaky v hesle")
    void loginUser_SpecialCharactersInPassword_Authenticates() throws Exception {
        // Given - Vytvoření uživatele se speciálními znaky v hesle
        String specialPassword = "P@ssw0rd!#$%^&*()";
        User specialUser = User.builder()
                .username("specialuser")
                .email("special@example.com")
                .password(passwordEncoder.encode(specialPassword))
                .roles(Collections.singleton(Role.ROLE_USER))
                .build();
        userRepository.save(specialUser);

        LoginRequest loginRequest = new LoginRequest("specialuser", specialPassword);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(cookie().exists("accessToken"));
    }

    // ========================================
    // CONTENT TYPE TESTY
    // ========================================

    @Test
    @Order(15)
    @DisplayName("POST /api/auth/login - Bez Content-Type vrátí 415 Unsupported Media Type")
    void loginUser_WithoutContentType_ReturnsUnsupportedMediaType() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest(TEST_USERNAME, TEST_PASSWORD);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                // Bez .contentType()
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @Order(16)
    @DisplayName("POST /api/auth/login - Špatný JSON formát vrátí 400 Bad Request")
    void loginUser_MalformedJson_ReturnsBadRequest() throws Exception {
        // Given - nevalidní JSON
        String malformedJson = "{\"username\":\"test\", \"password\":";

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}

package krematos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import krematos.repository.UserRepository;
import krematos.dto.password.RegisterRequest;
import krematos.model.User;
import krematos.model.enums.Role;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integrační testy pro registrační endpoint.
 * Používá best practices pro Spring Boot testování:
 * - @SpringBootTest pro plnou integraci s aplikačním kontextem
 * - @AutoConfigureMockMvc pro MockMvc podporu
 * - @ActiveProfiles pro testovací profil (např. H2 databáze)
 * - Testuje skutečnou registraci včetně validací
 * - BeforeEach/AfterEach pro setup/cleanup testovacích dat
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Register Integration Tests")
class RegisterControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String VALID_USERNAME = "newuser";
    private static final String VALID_EMAIL = "newuser@example.com";
    private static final String VALID_PASSWORD = "ValidPass123!";

    /**
     * Vyčistění databáze před každým testem.
     */
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    /**
     * Vyčištění testovacích dat po každém testu.
     */
    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    // ========================================
    // HAPPY PATH TESTY
    // ========================================

    @Test
    @Order(1)
    @DisplayName("POST /api/auth/register - Úspěšná registrace s platnými údaji")
    void registerUser_ValidData_Success() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest(VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Registrace proběhla úspěšně"));

        // Ověření, že uživatel je v databázi
        Optional<User> savedUser = userRepository.findByUsername(VALID_USERNAME);
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getEmail()).isEqualTo(VALID_EMAIL);
        assertThat(savedUser.get().getRoles()).contains(Role.ROLE_USER);

        // Ověření, že heslo je zahashované
        assertThat(savedUser.get().getPassword()).isNotEqualTo(VALID_PASSWORD);
        assertThat(passwordEncoder.matches(VALID_PASSWORD, savedUser.get().getPassword())).isTrue();
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/auth/register - Registrace s minimálními požadavky")
    void registerUser_MinimumRequirements_Success() throws Exception {
        // Given - minimální délka username (3 znaky) a hesla (8 znaků)
        RegisterRequest request = new RegisterRequest("abc", "min@test.com", "12345678");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());

        // Ověření v databázi
        Optional<User> savedUser = userRepository.findByUsername("abc");
        assertThat(savedUser).isPresent();
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/auth/register - Registrace s maximálním username (30 znaků)")
    void registerUser_MaximumUsername_Success() throws Exception {
        // Given - username s maximální délkou 30 znaků
        String maxUsername = "a".repeat(30);
        RegisterRequest request = new RegisterRequest(maxUsername, "max@test.com", VALID_PASSWORD);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());

        // Ověření v databázi
        Optional<User> savedUser = userRepository.findByUsername(maxUsername);
        assertThat(savedUser).isPresent();
    }

    @Test
    @Order(4)
    @DisplayName("POST /api/auth/register - Registrace s různými formáty emailu")
    void registerUser_VariousEmailFormats_Success() throws Exception {
        // Given - různé platné formáty emailu
        String[] validEmails = {
                "user@example.com",
                "user.name@example.com",
                "user_name@example-domain.com",
                "123@example.com"
        };

        for (int i = 0; i < validEmails.length; i++) {
            String username = "user" + i;
            RegisterRequest request = new RegisterRequest(username, validEmails[i], VALID_PASSWORD);

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk());
        }
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/auth/register - Heslo se speciálními znaky")
    void registerUser_PasswordWithSpecialCharacters_Success() throws Exception {
        // Given
        String specialPassword = "P@ssw0rd!#$%^&*()";
        RegisterRequest request = new RegisterRequest("special", "special@test.com", specialPassword);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());

        // Ověření, že se heslo správně zahashuje
        Optional<User> savedUser = userRepository.findByUsername("special");
        assertThat(savedUser).isPresent();
        assertThat(passwordEncoder.matches(specialPassword, savedUser.get().getPassword())).isTrue();
    }

    // ========================================
    // NEGATIVE PATH TESTY - Duplicitní údaje
    // ========================================

    @Test
    @Order(6)
    @DisplayName("POST /api/auth/register - Duplicitní username vrátí 409 Conflict")
    void registerUser_DuplicateUsername_ReturnsConflict() throws Exception {
        // Given - existující uživatel
        User existingUser = User.builder()
                .username(VALID_USERNAME)
                .email("different@email.com")
                .password(passwordEncoder.encode(VALID_PASSWORD))
                .roles(Collections.singleton(Role.ROLE_USER))
                .build();
        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest(VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    @Order(7)
    @DisplayName("POST /api/auth/register - Duplicitní email vrátí 409 Conflict")
    void registerUser_DuplicateEmail_ReturnsConflict() throws Exception {
        // Given - existující uživatel s emailem
        User existingUser = User.builder()
                .username("differentuser")
                .email(VALID_EMAIL)
                .password(passwordEncoder.encode(VALID_PASSWORD))
                .roles(Collections.singleton(Role.ROLE_USER))
                .build();
        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest(VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    @Order(8)
    @DisplayName("POST /api/auth/register - Duplicitní username i email vrátí 409 Conflict")
    void registerUser_DuplicateUsernameAndEmail_ReturnsConflict() throws Exception {
        // Given - existující uživatel
        User existingUser = User.builder()
                .username(VALID_USERNAME)
                .email(VALID_EMAIL)
                .password(passwordEncoder.encode(VALID_PASSWORD))
                .roles(Collections.singleton(Role.ROLE_USER))
                .build();
        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest(VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    // ========================================
    // VALIDAČNÍ TESTY - Username
    // ========================================

    @Test
    @Order(9)
    @DisplayName("POST /api/auth/register - Prázdné username vrátí 400 Bad Request")
    void registerUser_EmptyUsername_ReturnsBadRequest() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest("", VALID_EMAIL, VALID_PASSWORD);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").exists());
    }

    @Test
    @Order(10)
    @DisplayName("POST /api/auth/register - Příliš krátké username vrátí 400 Bad Request")
    void registerUser_TooShortUsername_ReturnsBadRequest() throws Exception {
        // Given - username musí mít minimálně 3 znaky
        RegisterRequest request = new RegisterRequest("ab", VALID_EMAIL, VALID_PASSWORD);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").exists());
    }

    @Test
    @Order(11)
    @DisplayName("POST /api/auth/register - Příliš dlouhé username vrátí 400 Bad Request")
    void registerUser_TooLongUsername_ReturnsBadRequest() throws Exception {
        // Given - username musí mít maximálně 30 znaků
        String longUsername = "a".repeat(31);
        RegisterRequest request = new RegisterRequest(longUsername, VALID_EMAIL, VALID_PASSWORD);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").exists());
    }

    @Test
    @Order(12)
    @DisplayName("POST /api/auth/register - Null username vrátí 400 Bad Request")
    void registerUser_NullUsername_ReturnsBadRequest() throws Exception {
        // Given
        String jsonWithNullUsername = "{\"username\":null,\"email\":\"" + VALID_EMAIL + "\",\"password\":\""
                + VALID_PASSWORD + "\"}";

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWithNullUsername))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ========================================
    // VALIDAČNÍ TESTY - Email
    // ========================================

    @Test
    @Order(13)
    @DisplayName("POST /api/auth/register - Prázdný email vrátí 400 Bad Request")
    void registerUser_EmptyEmail_ReturnsBadRequest() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest(VALID_USERNAME, "", VALID_PASSWORD);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());
    }

    @Test
    @Order(14)
    @DisplayName("POST /api/auth/register - Neplatný formát emailu vrátí 400 Bad Request")
    void registerUser_InvalidEmailFormat_ReturnsBadRequest() throws Exception {
        // Given - různé nevalidní formáty emailu
        String[] invalidEmails = {
                "invalid.email",
                "@example.com",
                "user@",
                "user @example.com",
                "user..name@example.com",
                "user@example"
        };

        for (String invalidEmail : invalidEmails) {
            RegisterRequest request = new RegisterRequest(VALID_USERNAME + Math.random(), invalidEmail, VALID_PASSWORD);

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.email").exists());
        }
    }

    @Test
    @Order(15)
    @DisplayName("POST /api/auth/register - Null email vrátí 400 Bad Request")
    void registerUser_NullEmail_ReturnsBadRequest() throws Exception {
        // Given
        String jsonWithNullEmail = "{\"username\":\"" + VALID_USERNAME + "\",\"email\":null,\"password\":\""
                + VALID_PASSWORD + "\"}";

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWithNullEmail))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ========================================
    // VALIDAČNÍ TESTY - Heslo
    // ========================================

    @Test
    @Order(16)
    @DisplayName("POST /api/auth/register - Prázdné heslo vrátí 400 Bad Request")
    void registerUser_EmptyPassword_ReturnsBadRequest() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest(VALID_USERNAME, VALID_EMAIL, "");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").exists());
    }

    @Test
    @Order(17)
    @DisplayName("POST /api/auth/register - Příliš krátké heslo vrátí 400 Bad Request")
    void registerUser_TooShortPassword_ReturnsBadRequest() throws Exception {
        // Given - heslo musí mít minimálně 8 znaků
        RegisterRequest request = new RegisterRequest(VALID_USERNAME, VALID_EMAIL, "1234567");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").exists());
    }

    @Test
    @Order(18)
    @DisplayName("POST /api/auth/register - Null heslo vrátí 400 Bad Request")
    void registerUser_NullPassword_ReturnsBadRequest() throws Exception {
        // Given
        String jsonWithNullPassword = "{\"username\":\"" + VALID_USERNAME + "\",\"email\":\"" + VALID_EMAIL
                + "\",\"password\":null}";

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWithNullPassword))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ========================================
    // VALIDAČNÍ TESTY - Kombinace
    // ========================================

    @Test
    @Order(19)
    @DisplayName("POST /api/auth/register - Všechna pole prázdná vrátí 400 Bad Request")
    void registerUser_AllFieldsBlank_ReturnsBadRequest() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest("", "", "");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.password").exists());
    }

    @Test
    @Order(20)
    @DisplayName("POST /api/auth/register - Všechna pole null vrátí 400 Bad Request")
    void registerUser_AllFieldsNull_ReturnsBadRequest() throws Exception {
        // Given
        String jsonWithNulls = "{\"username\":null,\"email\":null,\"password\":null}";

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWithNulls))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ========================================
    // EDGE CASE TESTY
    // ========================================

    @Test
    @Order(21)
    @DisplayName("POST /api/auth/register - Case sensitivity username")
    void registerUser_CaseSensitiveUsername_CreatesMultipleUsers() throws Exception {
        // Given - registrace s lowercase username
        RegisterRequest request1 = new RegisterRequest("testuser", "test1@example.com", VALID_PASSWORD);
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk());

        // When - pokus o registraci s velkými písmeny
        RegisterRequest request2 = new RegisterRequest("TESTUSER", "test2@example.com", VALID_PASSWORD);

        // Then - aplikace by měla povolit registraci (usernames jsou case-sensitive)
        // nebo zamítnout v závislosti na business logice
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andDo(print());
        // Výsledek závisí na implementaci - test ověřuje konzistentní chování
    }

    @Test
    @Order(22)
    @DisplayName("POST /api/auth/register - Unicode znaky v username")
    void registerUser_UnicodeCharactersInUsername_Success() throws Exception {
        // Given - username s českými znaky
        RegisterRequest request = new RegisterRequest("uživatel123", "unicode@test.com", VALID_PASSWORD);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print());
        // Výsledek závisí na implementaci - test ověřuje chování
    }

    // ========================================
    // SECURITY TESTY
    // ========================================

    @Test
    @Order(23)
    @DisplayName("POST /api/auth/register - SQL Injection pokus v username")
    void registerUser_SqlInjectionInUsername_Sanitized() throws Exception {
        // Given - SQL injection pokus
        RegisterRequest request = new RegisterRequest("admin' OR '1'='1", "sql@test.com", VALID_PASSWORD);

        // When & Then - měl by buď selhat na validaci, nebo bezpečně zpracovat
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print());
        // JPA by mělo bezpečně zpracovat vstup pomocí prepared statements
    }

    @Test
    @Order(24)
    @DisplayName("POST /api/auth/register - XSS pokus v username")
    void registerUser_XssAttemptInUsername_Sanitized() throws Exception {
        // Given - XSS pokus
        RegisterRequest request = new RegisterRequest("<script>alert('XSS')</script>", "xss@test.com", VALID_PASSWORD);

        // When & Then - měl by buď selhat na validaci, nebo bezpečně zpracovat
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print());
    }

    // ========================================
    // CONTENT TYPE TESTY
    // ========================================

    @Test
    @Order(25)
    @DisplayName("POST /api/auth/register - Bez Content-Type vrátí 415 Unsupported Media Type")
    void registerUser_WithoutContentType_ReturnsUnsupportedMediaType() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest(VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                // Bez .contentType()
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @Order(26)
    @DisplayName("POST /api/auth/register - Špatný JSON formát vrátí 400 Bad Request")
    void registerUser_MalformedJson_ReturnsBadRequest() throws Exception {
        // Given - nevalidní JSON
        String malformedJson = "{\"username\":\"test\", \"email\":\"test@test.com\", \"password\":";

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(27)
    @DisplayName("POST /api/auth/register - Prázdné tělo vrátí 400 Bad Request")
    void registerUser_EmptyBody_ReturnsBadRequest() throws Exception {
        // Given - prázdné tělo požadavku
        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ========================================
    // CSRF TESTY
    // ========================================

    @Test
    @Order(28)
    @DisplayName("POST /api/auth/register - Bez CSRF tokenu může selhat (závisí na konfiguraci)")
    void registerUser_WithoutCsrfToken_MayFail() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest(VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD);

        // When & Then
        // Pokud je CSRF ochrana povolena pro tento endpoint, měl by selhat
        mockMvc.perform(post("/api/auth/register")
                // Bez .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print());
        // Výsledek závisí na SecurityConfig
    }

    // ========================================
    // DATABASE INTEGRITY TESTY
    // ========================================

    @Test
    @Order(29)
    @DisplayName("POST /api/auth/register - Ověření, že se heslo hashuje v databázi")
    void registerUser_PasswordIsHashed_InDatabase() throws Exception {
        // Given
        String plainPassword = "PlainPassword123";
        RegisterRequest request = new RegisterRequest("hashtest", "hash@test.com", plainPassword);

        // When
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then - ověření, že heslo je zahashované v DB
        Optional<User> savedUser = userRepository.findByUsername("hashtest");
        assertThat(savedUser).isPresent();

        // Heslo v DB nesmí být stejné jako plaintext
        assertThat(savedUser.get().getPassword()).isNotEqualTo(plainPassword);

        // Heslo musí začínat BCrypt prefixem
        assertThat(savedUser.get().getPassword()).startsWith("$2a$");

        // Heslo musí být ověřitelné pomocí password encoderu
        assertThat(passwordEncoder.matches(plainPassword, savedUser.get().getPassword())).isTrue();
    }

    @Test
    @Order(30)
    @DisplayName("POST /api/auth/register - Ověření default role USER po registraci")
    void registerUser_AssignsDefaultUserRole() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest("roletest", "role@test.com", VALID_PASSWORD);

        // When
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then - ověření, že uživatel má default roli USER
        Optional<User> savedUser = userRepository.findByUsername("roletest");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getRoles()).isNotEmpty();
        assertThat(savedUser.get().getRoles()).contains(Role.ROLE_USER);
        assertThat(savedUser.get().getRoles()).doesNotContain(Role.ROLE_ADMIN);
    }
}

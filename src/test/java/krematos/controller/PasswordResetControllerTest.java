package krematos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import krematos.config.SecurityConfig;
import krematos.dto.password.ForgotPasswordRequest;
import krematos.dto.password.ResetPasswordRequest;
import krematos.service.JwtService;
import krematos.service.PasswordResetService;
import krematos.service.impl.UserDetailsServiceImpl;
import krematos.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test pro PasswordResetController.
 * Testuje REST API endpointy pro reset hesla.
 */
@WebMvcTest(PasswordResetController.class)
@Import({SecurityConfig.class})
@DisplayName("PasswordResetController Integration Tests")
class PasswordResetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean(name = "userService")
    private UserService userService;

    @MockBean
    private PasswordResetService passwordResetService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private ForgotPasswordRequest validForgotPasswordRequest;
    private ResetPasswordRequest validResetPasswordRequest;

    @BeforeEach
    void setUp() {
        // Příprava validních request objektů
        validForgotPasswordRequest = new ForgotPasswordRequest("user@example.com");
        validResetPasswordRequest = new ResetPasswordRequest("valid-token-123", "newPassword123");
    }

    // ==================== POST /api/auth/forgot-password ====================

    @Test
    @DisplayName("POST /api/auth/forgot-password - Úspěšný požadavek na reset hesla")
    void forgotPassword_ValidEmail_ReturnsSuccessMessage() throws Exception {
        // Given
        doNothing().when(passwordResetService).initiatePasswordReset(anyString());

        // When & Then
        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validForgotPasswordRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Žádost o obnovení hesla byla odeslána na váš email"));

        verify(passwordResetService, times(1)).initiatePasswordReset("user@example.com");
    }

    @Test
    @DisplayName("POST /api/auth/forgot-password - Neplatný formát emailu vrátí 400 Bad Request")
    void forgotPassword_InvalidEmailFormat_ReturnsBadRequest() throws Exception {
        // Given
        ForgotPasswordRequest invalidRequest = new ForgotPasswordRequest("invalid-email");

        // When & Then
        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(passwordResetService, never()).initiatePasswordReset(anyString());
    }

    @Test
    @DisplayName("POST /api/auth/forgot-password - Prázdný email vrátí 400 Bad Request")
    void forgotPassword_EmptyEmail_ReturnsBadRequest() throws Exception {
        // Given
        ForgotPasswordRequest emptyEmailRequest = new ForgotPasswordRequest("");

        // When & Then
        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyEmailRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(passwordResetService, never()).initiatePasswordReset(anyString());
    }

    @Test
    @DisplayName("POST /api/auth/forgot-password - Email bez @ znaku vrátí 400 Bad Request")
    void forgotPassword_EmailWithoutAtSign_ReturnsBadRequest() throws Exception {
        // Given
        ForgotPasswordRequest invalidRequest = new ForgotPasswordRequest("userexample.com");

        // When & Then
        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(passwordResetService, never()).initiatePasswordReset(anyString());
    }

    @Test
    @DisplayName("POST /api/auth/forgot-password - Request bez body vrátí 400 Bad Request")
    void forgotPassword_NoRequestBody_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(passwordResetService, never()).initiatePasswordReset(anyString());
    }

    // ==================== POST /api/auth/reset-password ====================

    @Test
    @DisplayName("POST /api/auth/reset-password - Úspěšný reset hesla")
    void resetPassword_ValidTokenAndPassword_ReturnsSuccessMessage() throws Exception {
        // Given
        doNothing().when(passwordResetService).resetPassword(anyString(), anyString());

        // When & Then
        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validResetPasswordRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Heslo bylo úspěšně změněno."));

        verify(passwordResetService, times(1)).resetPassword("valid-token-123", "newPassword123");
    }

    @Test
    @DisplayName("POST /api/auth/reset-password - Neplatný token vrátí 400 Bad Request")
    void resetPassword_InvalidToken_ReturnsBadRequest() throws Exception {
        // Given
        doThrow(new IllegalArgumentException("Neplatný token"))
                .when(passwordResetService).resetPassword(anyString(), anyString());

        // When & Then
        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validResetPasswordRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Neplatný token"));

        verify(passwordResetService, times(1)).resetPassword(anyString(), anyString());
    }

    @Test
    @DisplayName("POST /api/auth/reset-password - Vypršelý token vrátí 400 Bad Request")
    void resetPassword_ExpiredToken_ReturnsBadRequest() throws Exception {
        // Given
        doThrow(new IllegalArgumentException("Token vypršel"))
                .when(passwordResetService).resetPassword(anyString(), anyString());

        // When & Then
        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validResetPasswordRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Token vypršel"));

        verify(passwordResetService, times(1)).resetPassword(anyString(), anyString());
    }

    @Test
    @DisplayName("POST /api/auth/reset-password - Příliš krátké heslo vrátí 400 Bad Request")
    void resetPassword_PasswordTooShort_ReturnsBadRequest() throws Exception {
        // Given - heslo s méně než 8 znaky
        ResetPasswordRequest shortPasswordRequest = new ResetPasswordRequest("valid-token", "short");

        // When & Then
        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shortPasswordRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(passwordResetService, never()).resetPassword(anyString(), anyString());
    }

    @Test
    @DisplayName("POST /api/auth/reset-password - Prázdné heslo vrátí 400 Bad Request")
    void resetPassword_EmptyPassword_ReturnsBadRequest() throws Exception {
        // Given
        ResetPasswordRequest emptyPasswordRequest = new ResetPasswordRequest("valid-token", "");

        // When & Then
        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyPasswordRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(passwordResetService, never()).resetPassword(anyString(), anyString());
    }

    @Test
    @DisplayName("POST /api/auth/reset-password - Prázdný token vrátí 400 Bad Request")
    void resetPassword_EmptyToken_ReturnsBadRequest() throws Exception {
        // Given
        ResetPasswordRequest emptyTokenRequest = new ResetPasswordRequest("", "validPassword123");

        // When & Then
        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyTokenRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(passwordResetService, never()).resetPassword(anyString(), anyString());
    }

    @Test
    @DisplayName("POST /api/auth/reset-password - Request bez body vrátí 400 Bad Request")
    void resetPassword_NoRequestBody_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(passwordResetService, never()).resetPassword(anyString(), anyString());
    }

    @Test
    @DisplayName("POST /api/auth/reset-password - Null hodnoty vrátí 400 Bad Request")
    void resetPassword_NullValues_ReturnsBadRequest() throws Exception {
        // Given - JSON s null hodnotami
        String nullValuesJson = "{\"token\": null, \"newPassword\": null}";

        // When & Then
        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(nullValuesJson))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(passwordResetService, never()).resetPassword(anyString(), anyString());
    }
}

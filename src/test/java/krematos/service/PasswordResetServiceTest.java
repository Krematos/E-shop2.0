package krematos.service;

import krematos.model.PasswordResetToken;
import krematos.model.User;
import krematos.repository.PasswordResetRepository;
import krematos.repository.UserRepository;
import krematos.service.email.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordResetService Tests")
@ActiveProfiles("test")
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordResetRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_TOKEN = "test-token-123";
    private static final String NEW_PASSWORD = "newPassword123";
    private static final String ENCODED_PASSWORD = "encodedPassword123";

    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail(TEST_EMAIL);
        user.setPassword("oldPassword123");
        return user;
    }

    @Nested
    @DisplayName("initiatePasswordReset Tests")
    class InitiatePasswordResetTests {

        @Test
        @DisplayName("Zpracování platného e-mailu uloží token a odešle email")
        void shouldInitiatePasswordReset_SendsEmailAndSavesToken() {
            // Given
            User user = createTestUser();
            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
            when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(i -> i.getArguments()[0]);

            // When
            passwordResetService.initiatePasswordReset(TEST_EMAIL);

            // Then
            ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
            verify(tokenRepository).save(tokenCaptor.capture());
            verify(emailService).sendPasswordResetEmail(eq(TEST_EMAIL), anyString());

            PasswordResetToken savedToken = tokenCaptor.getValue();
            assertThat(savedToken.getUser()).isEqualTo(user);
            assertThat(savedToken.getToken()).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("Zpracování neplatného e-mailu neudělá nic (ochrana proti User Enumeration)")
        void shouldDoNothing_WhenUserDoesNotExist() {
            // Given
            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

            // When
            passwordResetService.initiatePasswordReset(TEST_EMAIL);

            // Then
            verify(tokenRepository, never()).save(any(PasswordResetToken.class));
            verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("resetPassword Tests")
    class ResetPasswordTests {

        @Test
        @DisplayName("Úspěšně resetuje heslo, pokud je token platný")
        void shouldResetPassword_WhenTokenIsValid() {
            // Given
            User user = createTestUser();
            PasswordResetToken resetToken = new PasswordResetToken(TEST_TOKEN, user);
            resetToken.setExpiryDate(Instant.now().plus(1, ChronoUnit.HOURS));

            when(tokenRepository.findByToken(TEST_TOKEN)).thenReturn(Optional.of(resetToken));
            when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(ENCODED_PASSWORD);

            // When
            passwordResetService.resetPassword(TEST_TOKEN, NEW_PASSWORD);

            // Then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();

            assertThat(savedUser.getPassword()).isEqualTo(ENCODED_PASSWORD);
            verify(tokenRepository).delete(resetToken);
        }

        @Test
        @DisplayName("Vyhodí výjimku, pokud token neexistuje")
        void shouldThrowException_WhenTokenIsInvalid() {
            // Given
            when(tokenRepository.findByToken(TEST_TOKEN)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> passwordResetService.resetPassword(TEST_TOKEN, NEW_PASSWORD))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Neplatný token");

            verify(userRepository, never()).save(any(User.class));
            verify(tokenRepository, never()).delete(any(PasswordResetToken.class));
        }

        @Test
        @DisplayName("Vyhodí výjimku, pokud token vypršel, a smaže jej z DB")
        void shouldThrowExceptionAndCleanUp_WhenTokenIsExpired() {
            // Given
            User user = createTestUser();
            PasswordResetToken resetToken = new PasswordResetToken(TEST_TOKEN, user);
            resetToken.setExpiryDate(Instant.now().minus(1, ChronoUnit.HOURS)); // Token už vypršel

            when(tokenRepository.findByToken(TEST_TOKEN)).thenReturn(Optional.of(resetToken));

            // When & Then
            assertThatThrownBy(() -> passwordResetService.resetPassword(TEST_TOKEN, NEW_PASSWORD))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Token vypršel");

            verify(userRepository, never()).save(any(User.class));
            verify(tokenRepository).delete(resetToken);
        }
    }
}

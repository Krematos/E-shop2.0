package krematos.service;

import io.jsonwebtoken.Claims;
import krematos.exception.token.InvalidTokenException;
import krematos.model.BlacklistedToken;
import krematos.repository.BlacklistedTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit testy pro JwtService s důrazem na security.
 * Testuje generování, validaci, expiraci a blacklisting JWT tokenů.
 */
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
@DisplayName("JwtService Tests")
class JwtServiceTest {

    @Mock
    private BlacklistedTokenRepository blacklistedTokenRepository;

    @Mock
    private Clock clock;
    private JwtService jwtService;

    private static final String TEST_USERNAME = "testuser";
    private static final String VALID_SECRET = "thisIsAVerySecureSecretKeyForJwtTokenGenerationAndValidation123456";
    private static final long ACCESS_TOKEN_EXPIRATION = 900000L; // 15 min
    private static final long REFRESH_TOKEN_EXPIRATION = 604800000L; // 7 days

    private static final Instant FIXED_INSTANT = Instant.parse("2030-01-01T12:00:00Z");

    @BeforeEach
    void setUp() {
        // Mock clock to return fixed instant for consistent test behavior
        when(clock.instant()).thenReturn(FIXED_INSTANT);
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        jwtService = new JwtService(
                VALID_SECRET,
                ACCESS_TOKEN_EXPIRATION,
                REFRESH_TOKEN_EXPIRATION,
                blacklistedTokenRepository,
                clock);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Měl by vyhodit IllegalArgumentException pro příliš krátký secret")
        void shouldThrowException_WhenSecretTooShort() {
            // Given
            String shortSecret = "short"; // Méně než 32 znaků

            // When & Then
            assertThatThrownBy(() -> new JwtService(
                    shortSecret,
                    ACCESS_TOKEN_EXPIRATION,
                    REFRESH_TOKEN_EXPIRATION,
                    blacklistedTokenRepository,
                    clock))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("JWT secret must be at least 32 characters");
        }

        @Test
        @DisplayName("Měl by vyhodit IllegalArgumentException pro null secret")
        void shouldThrowException_WhenSecretIsNull() {
            // When & Then
            assertThatThrownBy(() -> new JwtService(
                    null,
                    ACCESS_TOKEN_EXPIRATION,
                    REFRESH_TOKEN_EXPIRATION,
                    blacklistedTokenRepository,
                    clock))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("JWT secret must be at least 32 characters");
        }

        @Test
        @DisplayName("Měl by vyhodit IllegalArgumentException pro prázdný secret")
        void shouldThrowException_WhenSecretIsBlank() {
            // When & Then
            assertThatThrownBy(() -> new JwtService(
                    "   ",
                    ACCESS_TOKEN_EXPIRATION,
                    REFRESH_TOKEN_EXPIRATION,
                    blacklistedTokenRepository,
                    clock))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("JWT secret must be at least 32 characters");
        }

        @Test
        @DisplayName("Měl by úspěšně vytvořit instanci s validním secretem")
        void shouldCreateInstance_WithValidSecret() {
            // When & Then
            assertThatCode(() -> new JwtService(
                    VALID_SECRET,
                    ACCESS_TOKEN_EXPIRATION,
                    REFRESH_TOKEN_EXPIRATION,
                    blacklistedTokenRepository,
                    clock)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("generateAccessToken Tests")
    class GenerateAccessTokenTests {

        @Test
        @DisplayName("Měl by vygenerovat validní access token")
        void shouldGenerateValidAccessToken() {
            // When
            String token = jwtService.generateAccessToken(TEST_USERNAME);

            // Then
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3); // JWT má 3 části: header.payload.signature
        }

        @Test
        @DisplayName("Měl by vygenerovat token s správným username")
        void shouldGenerateToken_WithCorrectUsername() {
            // When
            String token = jwtService.generateAccessToken(TEST_USERNAME);

            // Then
            String extractedUsername = jwtService.extractUsername(token);
            assertThat(extractedUsername).isEqualTo(TEST_USERNAME);
        }

        @Test
        @DisplayName("Měl by vygenerovat různé tokeny pro stejného uživatele")
        void shouldGenerateDifferentTokens_ForSameUser() {
            // Given - První token s časem T
            Instant firstInstant = Instant.parse("2026-01-24T20:00:00Z");
            Clock firstClock = Clock.fixed(firstInstant, ZoneId.systemDefault());
            JwtService serviceWithFirstTime = new JwtService(
                    VALID_SECRET,
                    ACCESS_TOKEN_EXPIRATION,
                    REFRESH_TOKEN_EXPIRATION,
                    blacklistedTokenRepository,
                    firstClock);

            String token1 = serviceWithFirstTime.generateAccessToken(TEST_USERNAME);

            // Given - Druhý token s časem T+1 hodina
            Instant secondInstant = firstInstant.plusSeconds(3600);
            Clock secondClock = Clock.fixed(secondInstant, ZoneId.systemDefault());
            JwtService serviceWithSecondTime = new JwtService(
                    VALID_SECRET,
                    ACCESS_TOKEN_EXPIRATION,
                    REFRESH_TOKEN_EXPIRATION,
                    blacklistedTokenRepository,
                    secondClock);

            String token2 = serviceWithSecondTime.generateAccessToken(TEST_USERNAME);

            // Then
            assertThat(token1).isNotEqualTo(token2);
        }
    }

    @Nested
    @DisplayName("generateRefreshToken Tests")
    class GenerateRefreshTokenTests {

        @Test
        @DisplayName("Měl by vygenerovat validní refresh token")
        void shouldGenerateValidRefreshToken() {
            // When
            String token = jwtService.generateRefreshToken(TEST_USERNAME);

            // Then
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
        }

        @Test
        @DisplayName("Refresh token by měl mít delší expiraci než access token")
        void refreshTokenShouldHaveLongerExpiration() {
            // When
            String accessToken = jwtService.generateAccessToken(TEST_USERNAME);
            String refreshToken = jwtService.generateRefreshToken(TEST_USERNAME);

            // Then
            Date accessExpiration = jwtService.extractClaim(accessToken, Claims::getExpiration);
            Date refreshExpiration = jwtService.extractClaim(refreshToken, Claims::getExpiration);

            assertThat(refreshExpiration).isAfter(accessExpiration);
        }
    }

    @Nested
    @DisplayName("validateToken Tests")
    class ValidateTokenTests {

        @Test
        @DisplayName("Měl by validovat správný token")
        void shouldValidateCorrectToken() {
            // Given
            String token = jwtService.generateAccessToken(TEST_USERNAME);

            // When
            boolean isValid = jwtService.validateToken(token, TEST_USERNAME);

            // Then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Měl by odmítnout token s jiným username")
        void shouldRejectToken_WithDifferentUsername() {
            // Given
            String token = jwtService.generateAccessToken(TEST_USERNAME);

            // When
            boolean isValid = jwtService.validateToken(token, "differentUser");

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Měl by odmítnout neplatný token")
        void shouldRejectInvalidToken() {
            // Given
            String invalidToken = "invalid.token.here";

            // When & Then
            assertThatThrownBy(() -> jwtService.validateToken(invalidToken, TEST_USERNAME))
                    .isInstanceOf(InvalidTokenException.class)
                    .hasMessage("Neplatný token JWT");
        }
    }

    @Nested
    @DisplayName("extractUsername Tests")
    class ExtractUsernameTests {

        @Test
        @DisplayName("Měl by extrahovat username z validního tokenu")
        void shouldExtractUsername_FromValidToken() {
            // Given
            String token = jwtService.generateAccessToken(TEST_USERNAME);

            // When
            String username = jwtService.extractUsername(token);

            // Then
            assertThat(username).isEqualTo(TEST_USERNAME);
        }

        @Test
        @DisplayName("Měl by vyhodit InvalidTokenException pro neplatný token")
        void shouldThrowException_ForInvalidToken() {
            // Given
            String invalidToken = "invalid.token";

            // When & Then
            assertThatThrownBy(() -> jwtService.extractUsername(invalidToken))
                    .isInstanceOf(InvalidTokenException.class);
        }
    }

    @Nested
    @DisplayName("extractClaim Tests")
    class ExtractClaimTests {

        @Test
        @DisplayName("Měl by extrahovat expiration claim")
        void shouldExtractExpirationClaim() {
            // Given
            String token = jwtService.generateAccessToken(TEST_USERNAME);

            // When
            Date expiration = jwtService.extractClaim(token, Claims::getExpiration);

            // Then
            Date expectedExpiration = Date.from(FIXED_INSTANT.plusMillis(ACCESS_TOKEN_EXPIRATION));
            assertThat(expiration).isEqualTo(expectedExpiration);
        }

        @Test
        @DisplayName("Měl by extrahovat issued at claim")
        void shouldExtractIssuedAtClaim() {
            // Given
            String token = jwtService.generateAccessToken(TEST_USERNAME);

            // When
            Date issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);

            // Then
            Date expectedIssuedAt = Date.from(FIXED_INSTANT);
            assertThat(issuedAt).isEqualTo(expectedIssuedAt);
        }

        @Test
        @DisplayName("Měl by extrahovat subject claim")
        void shouldExtractSubjectClaim() {
            // Given
            String token = jwtService.generateAccessToken(TEST_USERNAME);

            // When
            String subject = jwtService.extractClaim(token, Claims::getSubject);

            // Then
            assertThat(subject).isEqualTo(TEST_USERNAME);
        }
    }

    @Nested
    @DisplayName("isTokenExpired Tests")
    class IsTokenExpiredTests {

        @Test
        @DisplayName("Měl by vrátit false pro platný token")
        void shouldReturnFalse_ForValidToken() {
            // Given
            String token = jwtService.generateAccessToken(TEST_USERNAME);

            // When
            Date currentTime = Date.from(FIXED_INSTANT);
            boolean isExpired = jwtService.extractClaim(token, Claims::getExpiration).before(currentTime);

            // Then
            assertThat(isExpired).isFalse();
        }
    }

    @Nested
    @DisplayName("blacklistToken Tests")
    class BlacklistTokenTests {

        @Test
        @DisplayName("Měl by přidat token na blacklist")
        void shouldAddTokenToBlacklist() {
            // Given
            String token = jwtService.generateRefreshToken(TEST_USERNAME);
            when(blacklistedTokenRepository.save(any(BlacklistedToken.class))).thenReturn(null);

            // When
            jwtService.blacklistToken(token);

            // Then
            ArgumentCaptor<BlacklistedToken> captor = ArgumentCaptor.forClass(BlacklistedToken.class);
            verify(blacklistedTokenRepository).save(captor.capture());

            BlacklistedToken blacklistedToken = captor.getValue();
            assertThat(blacklistedToken.getToken()).isEqualTo(token);
            assertThat(blacklistedToken.getExpirationDate()).isAfter(FIXED_INSTANT);
        }

        @Test
        @DisplayName("Měl by uložit expiraci tokenu při blacklistingu")
        void shouldSaveTokenExpiration_WhenBlacklisting() {
            // Given
            String token = jwtService.generateRefreshToken(TEST_USERNAME);
            Date tokenExpiration = jwtService.extractClaim(token, Claims::getExpiration);
            when(blacklistedTokenRepository.save(any(BlacklistedToken.class))).thenReturn(null);

            // When
            jwtService.blacklistToken(token);

            // Then
            ArgumentCaptor<BlacklistedToken> captor = ArgumentCaptor.forClass(BlacklistedToken.class);
            verify(blacklistedTokenRepository).save(captor.capture());

            BlacklistedToken blacklistedToken = captor.getValue();
            assertThat(blacklistedToken.getExpirationDate().toEpochMilli())
                    .isEqualTo(tokenExpiration.toInstant().toEpochMilli());
        }
    }

    @Nested
    @DisplayName("isTokenBlacklisted Tests")
    class IsTokenBlacklistedTests {

        @Test
        @DisplayName("Měl by vrátit true pro blacklistovaný token")
        void shouldReturnTrue_ForBlacklistedToken() {
            // Given
            String token = "blacklisted-token";
            when(blacklistedTokenRepository.existsByToken(token)).thenReturn(true);

            // When
            boolean isBlacklisted = jwtService.isTokenBlacklisted(token);

            // Then
            assertThat(isBlacklisted).isTrue();
            verify(blacklistedTokenRepository).existsByToken(token);
        }

        @Test
        @DisplayName("Měl by vrátit false pro neblacklistovaný token")
        void shouldReturnFalse_ForNonBlacklistedToken() {
            // Given
            String token = "valid-token";
            when(blacklistedTokenRepository.existsByToken(token)).thenReturn(false);

            // When
            boolean isBlacklisted = jwtService.isTokenBlacklisted(token);

            // Then
            assertThat(isBlacklisted).isFalse();
            verify(blacklistedTokenRepository).existsByToken(token);
        }
    }

    @Nested
    @DisplayName("extractRoles Tests")
    class ExtractRolesTests {

        @Test
        @DisplayName("Měl by vrátit prázdný seznam, když token nemá role")
        void shouldReturnEmptyList_WhenTokenHasNoRoles() {
            // Given
            String token = jwtService.generateAccessToken(TEST_USERNAME);

            // When
            List<String> roles = jwtService.extractRoles(token);

            // Then
            assertThat(roles).isEmpty();
        }

        @Test
        @DisplayName("Měl by bezpečně zpracovat token bez roles claim")
        void shouldSafelyHandleToken_WithoutRolesClaim() {
            // Given
            String token = jwtService.generateAccessToken(TEST_USERNAME);

            // When & Then
            assertThatCode(() -> jwtService.extractRoles(token))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Edge Cases and Security Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Měl by odmítnout token s pozměněným podpisem")
        void shouldRejectToken_WithTamperedSignature() {
            // Given
            String token = jwtService.generateAccessToken(TEST_USERNAME);
            String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";

            // When & Then
            assertThatThrownBy(() -> jwtService.validateToken(tamperedToken, TEST_USERNAME))
                    .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        @DisplayName("Měl by odmítnout prázdný token")
        void shouldRejectEmptyToken() {
            // When & Then
            assertThatThrownBy(() -> jwtService.extractUsername(""))
                    .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        @DisplayName("Měl by odmítnout null token")
        void shouldRejectNullToken() {
            // When & Then
            assertThatThrownBy(() -> jwtService.extractUsername(null))
                    .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        @DisplayName("Měl by generovat token s časem z Clock")
        void shouldGenerateToken_WithClockTime() {

            // When
            String token = jwtService.generateAccessToken(TEST_USERNAME);

            // Then
            Date issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);
            Date expectedIssuedAt = Date.from(FIXED_INSTANT);

            assertThat(issuedAt).isEqualTo(expectedIssuedAt);
        }
    }
}

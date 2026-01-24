package krematos.service;

import krematos.dto.user.UserUpdateResponse;
import krematos.event.UserRegisteredEvent;
import krematos.exception.email.EmailAlreadyExistsException;
import krematos.exception.user.UserAlreadyExistException;
import krematos.model.User;
import krematos.model.enums.Role;
import krematos.repository.UserRepository;
import krematos.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit testy pro UserService s použitím best practices.
 * Pokrývá všechny metody včetně edge cases a error handling.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserService userService;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "encodedPassword123";
    private static final Long USER_ID = 1L;

    // --- Helper Methods ---

    private User createTestUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setUsername(TEST_USERNAME);
        user.setEmail(TEST_EMAIL);
        user.setPassword(TEST_PASSWORD);
        user.setRoles(new HashSet<>(Collections.singletonList(Role.ROLE_USER)));
        return user;
    }

    private User createTestUserWithEncodedPassword() {
        User user = createTestUser();
        user.setPassword(ENCODED_PASSWORD);
        return user;
    }

    // --- Nested Test Classes ---

    @Nested
    @DisplayName("registerNewUser Tests")
    class RegisterNewUserTests {

        @Test
        @DisplayName("Měl by úspěšně zaregistrovat nového uživatele")
        void shouldRegisterNewUser_Successfully() {
            // Given
            User user = createTestUser();
            User savedUser = createTestUserWithEncodedPassword();

            when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
            when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            // When
            User result = userService.registerNewUser(user);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getPassword()).isEqualTo(ENCODED_PASSWORD);
            assertThat(result.getRoles()).contains(Role.ROLE_USER);

            verify(userRepository).existsByUsername(TEST_USERNAME);
            verify(userRepository).existsByEmail(TEST_EMAIL);
            verify(passwordEncoder).encode(TEST_PASSWORD);
            verify(userRepository).save(any(User.class));
            verify(eventPublisher).publishEvent(any(UserRegisteredEvent.class));
        }

        @Test
        @DisplayName("Měl by vyhodit UserAlreadyExistException pro duplicitní username")
        void shouldThrowUserAlreadyExistException_WhenUsernameExists() {
            // Given
            User user = createTestUser();
            when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> userService.registerNewUser(user))
                    .isInstanceOf(UserAlreadyExistException.class)
                    .hasMessage("Uživatelské jméno již existuje");

            verify(userRepository).existsByUsername(TEST_USERNAME);
            verify(userRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Měl by vyhodit EmailAlreadyExistsException pro duplicitní email")
        void shouldThrowEmailAlreadyExistsException_WhenEmailExists() {
            // Given
            User user = createTestUser();
            when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
            when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> userService.registerNewUser(user))
                    .isInstanceOf(EmailAlreadyExistsException.class)
                    .hasMessage("Email již existuje");

            verify(userRepository).existsByUsername(TEST_USERNAME);
            verify(userRepository).existsByEmail(TEST_EMAIL);
            verify(userRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Měl by nastavit výchozí roli ROLE_USER")
        void shouldSetDefaultRoleUser() {
            // Given
            User user = createTestUser();
            user.setRoles(null); // Žádné role nastaveny

            when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
            when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(userRepository.save(any(User.class))).thenReturn(user);

            // When
            User result = userService.registerNewUser(user);

            // Then
            assertThat(result.getRoles()).isNotNull();
            assertThat(result.getRoles()).hasSize(1);
            assertThat(result.getRoles()).contains(Role.ROLE_USER);
        }

        @Test
        @DisplayName("Měl by publikovat UserRegisteredEvent po úspěšné registraci")
        void shouldPublishUserRegisteredEvent_AfterSuccessfulRegistration() {
            // Given
            User user = createTestUser();
            User savedUser = createTestUserWithEncodedPassword();

            when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
            when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            // When
            userService.registerNewUser(user);

            // Then
            ArgumentCaptor<UserRegisteredEvent> eventCaptor = ArgumentCaptor.forClass(UserRegisteredEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            UserRegisteredEvent capturedEvent = eventCaptor.getValue();
            assertThat(capturedEvent.getUser()).isEqualTo(true);
        }
    }

    @Nested
    @DisplayName("updateUser Tests")
    class UpdateUserTests {

        @Test
        @DisplayName("Měl by úspěšně aktualizovat uživatele")
        void shouldUpdateUser_Successfully() {
            // Given
            User user = createTestUser();
            UserUpdateResponse updateResponse = new UserUpdateResponse("Vašek" , "Novák", "vnovak@gmail.com");
            User updatedUser = createTestUser();
            updatedUser.setEmail("newemail@example.com");

            when(userRepository.save(user)).thenReturn(updatedUser);

            // When
            User result = userService.updateUser(user, updateResponse);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("newemail@example.com");
            verify(userRepository).save(user);
        }
    }

    @Nested
    @DisplayName("createPasswordResetTokenForUser Tests")
    class CreatePasswordResetTokenTests {

        @Test
        @DisplayName("Měl by vytvořit reset token pro existujícího uživatele")
        void shouldCreatePasswordResetToken_ForExistingUser() {
            // Given
            User user = createTestUser();
            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);

            // When
            String token = userService.createPasswordResetTokenForUser(TEST_EMAIL);

            // Then
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getPasswordResetToken()).isEqualTo(token);
            assertThat(savedUser.getPasswordResetTokenExpiry()).isAfter(Instant.now());
        }

        @Test
        @DisplayName("Měl by vyhodit UsernameNotFoundException pro neexistující email")
        void shouldThrowUsernameNotFoundException_WhenEmailNotFound() {
            // Given
            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.createPasswordResetTokenForUser(TEST_EMAIL))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining("User not found with email");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Měl by nastavit expiraci tokenu na 15 minut")
        void shouldSetTokenExpiry_To15Minutes() {
            // Given
            User user = createTestUser();
            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);

            Instant beforeCreation = Instant.now();

            // When
            userService.createPasswordResetTokenForUser(TEST_EMAIL);

            // Then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            Instant expectedExpiry = beforeCreation.plus(15, ChronoUnit.MINUTES);

            assertThat(savedUser.getPasswordResetTokenExpiry())
                    .isAfterOrEqualTo(expectedExpiry.minus(1, ChronoUnit.SECONDS))
                    .isBeforeOrEqualTo(expectedExpiry.plus(1, ChronoUnit.SECONDS));
        }
    }

    @Nested
    @DisplayName("resetPassword Tests")
    class ResetPasswordTests {

        @Test
        @DisplayName("Měl by úspěšně resetovat heslo s platným tokenem")
        void shouldResetPassword_WithValidToken() {
            // Given
            String token = "valid-token";
            String newPassword = "newPassword123";
            User user = createTestUser();
            user.setPasswordResetToken(token);
            user.setPasswordResetTokenExpiry(Instant.now().plus(10, ChronoUnit.MINUTES));

            when(userRepository.findByPasswordResetToken(token)).thenReturn(Optional.of(user));
            when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");
            when(userRepository.save(any(User.class))).thenReturn(user);

            // When
            userService.resetPassword(token, newPassword);

            // Then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getPassword()).isEqualTo("encodedNewPassword");
            assertThat(savedUser.getPasswordResetToken()).isNull();
            assertThat(savedUser.getPasswordResetTokenExpiry()).isNull();
        }

        @Test
        @DisplayName("Měl by vyhodit IllegalArgumentException pro neplatný token")
        void shouldThrowIllegalArgumentException_ForInvalidToken() {
            // Given
            String invalidToken = "invalid-token";
            when(userRepository.findByPasswordResetToken(invalidToken)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.resetPassword(invalidToken, "newPassword"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid password reset token");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Měl by vyhodit IllegalArgumentException pro vypršelý token")
        void shouldThrowIllegalArgumentException_ForExpiredToken() {
            // Given
            String token = "expired-token";
            User user = createTestUser();
            user.setPasswordResetToken(token);
            user.setPasswordResetTokenExpiry(Instant.now().minus(1, ChronoUnit.HOURS)); // Vypršelý

            when(userRepository.findByPasswordResetToken(token)).thenReturn(Optional.of(user));

            // When & Then
            assertThatThrownBy(() -> userService.resetPassword(token, "newPassword"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Password reset token has expired");

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("changeUserRole Tests")
    class ChangeUserRoleTests {

        @Test
        @DisplayName("Měl by úspěšně změnit roli uživatele")
        void shouldChangeUserRole_Successfully() {
            // Given
            User user = createTestUser();
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);

            // When
            userService.changeUserRole(USER_ID, Role.ROLE_ADMIN);

            // Then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getRoles()).contains(Role.ROLE_ADMIN);
            assertThat(savedUser.getRoles()).hasSize(1);
        }

        @Test
        @DisplayName("Měl by vyhodit IllegalArgumentException pro neexistujícího uživatele")
        void shouldThrowIllegalArgumentException_WhenUserNotFound() {
            // Given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.changeUserRole(USER_ID, Role.ROLE_ADMIN))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("nebyl nalezen");

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteUserById Tests")
    class DeleteUserByIdTests {

        @Test
        @DisplayName("Měl by smazat uživatele podle ID")
        void shouldDeleteUser_ById() {
            // Given
            doNothing().when(userRepository).deleteById(USER_ID);

            // When
            userService.deleteUserById(USER_ID);

            // Then
            verify(userRepository).deleteById(USER_ID);
        }
    }

    @Nested
    @DisplayName("findUserByUsername Tests")
    class FindUserByUsernameTests {

        @Test
        @DisplayName("Měl by najít uživatele podle username")
        void shouldFindUser_ByUsername() {
            // Given
            User user = createTestUser();
            when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));

            // When
            Optional<User> result = userService.findUserByUsername(TEST_USERNAME);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getUsername()).isEqualTo(TEST_USERNAME);
            verify(userRepository).findByUsername(TEST_USERNAME);
        }

        @Test
        @DisplayName("Měl by vrátit prázdný Optional pro neexistujícího uživatele")
        void shouldReturnEmpty_WhenUserNotFound() {
            // Given
            when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());

            // When
            Optional<User> result = userService.findUserByUsername(TEST_USERNAME);

            // Then
            assertThat(result).isEmpty();
            verify(userRepository).findByUsername(TEST_USERNAME);
        }
    }

    @Nested
    @DisplayName("findUserById Tests")
    class FindUserByIdTests {

        @Test
        @DisplayName("Měl by najít uživatele podle ID")
        void shouldFindUser_ById() {
            // Given
            User user = createTestUser();
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

            // When
            Optional<User> result = userService.findUserById(USER_ID);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(USER_ID);
            verify(userRepository).findById(USER_ID);
        }
    }

    @Nested
    @DisplayName("findAllUsers Tests")
    class FindAllUsersTests {

        @Test
        @DisplayName("Měl by vrátit všechny uživatele")
        void shouldReturnAllUsers() {
            // Given
            List<User> users = Arrays.asList(createTestUser(), createTestUser());
            when(userRepository.findAll()).thenReturn(users);

            // When
            List<User> result = userService.findAllUsers();

            // Then
            assertThat(result).hasSize(2);
            verify(userRepository).findAll();
        }

        @Test
        @DisplayName("Měl by vrátit prázdný seznam, když neexistují žádní uživatelé")
        void shouldReturnEmptyList_WhenNoUsersExist() {
            // Given
            when(userRepository.findAll()).thenReturn(Collections.emptyList());

            // When
            List<User> result = userService.findAllUsers();

            // Then
            assertThat(result).isEmpty();
            verify(userRepository).findAll();
        }
    }

    @Nested
    @DisplayName("isOwner Tests")
    class IsOwnerTests {

        @Test
        @DisplayName("Měl by vrátit true, když uživatel je vlastník")
        void shouldReturnTrue_WhenUserIsOwner() {
            // Given
            User user = createTestUser();
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

            // When
            boolean result = userService.isOwner(USER_ID, TEST_USERNAME);

            // Then
            assertThat(result).isTrue();
            verify(userRepository).findById(USER_ID);
        }

        @Test
        @DisplayName("Měl by vrátit false, když uživatel není vlastník")
        void shouldReturnFalse_WhenUserIsNotOwner() {
            // Given
            User user = createTestUser();
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

            // When
            boolean result = userService.isOwner(USER_ID, "differentUsername");

            // Then
            assertThat(result).isFalse();
            verify(userRepository).findById(USER_ID);
        }

        @Test
        @DisplayName("Měl by vrátit false, když uživatel neexistuje")
        void shouldReturnFalse_WhenUserNotFound() {
            // Given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            // When
            boolean result = userService.isOwner(USER_ID, TEST_USERNAME);

            // Then
            assertThat(result).isFalse();
            verify(userRepository).findById(USER_ID);
        }
    }
}

package org.example.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.UserUpdateResponse;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.example.service.email.EmailService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.time.LocalDateTime;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    /**
     * Registrace nového uživatele s výchozí rolí USER.
     *
     * @param user Uživatel k registraci.
     * @return Registrovaný uživatel.
     * @throws IllegalArgumentException pokud uživatelské jméno nebo email již
     *                                  existuje.
     */
    @Transactional
    @CacheEvict(value = { "users", "usersById", "allUsers" }, allEntries = true)
    public User registerNewUser(User user) {
        validateUser(user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Set.of(User.Role.ROLE_USER)); // Výchozí role

        User savedUser = userRepository.save(user);

        log.info("Nový uživatel registrován: {}", savedUser.getUsername());

        // Odeslání uvítacího emailu
        emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getUsername());

        return savedUser;
    }

    @Transactional
    @CacheEvict(value = { "users", "usersById", "allUsers" }, allEntries = true)
    public User updateUser(User user, UserUpdateResponse userUpdateResponse) {
        // Assuming validateUserUpdate is a new method to be added or already exists
        // For now, commenting it out as it's not in the original document
        // validateUserUpdate(userUpdateResponse, user.getId());

        // UserUpdateResponse obsahuje firstName, lastName, email
        // ale User entita má pouze username, email, password, roles
        // Aktualizujeme pouze email
        user.setEmail(userUpdateResponse.email());

        User updatedUser = userRepository.save(user);
        log.info("Data uživatele s ID {} byla úspěšně aktualizována.", user.getId());
        return updatedUser;
    }

    /**
     * Vytvoření tokenu pro reset hesla a jeho přiřazení uživateli.
     *
     * @param email Email uživatele, který žádá o reset hesla.
     * @return Vygenerovaný token pro reset hesla.
     * @throws UsernameNotFoundException pokud uživatel s daným emailem neexistuje.
     */
    @Transactional
    public String createPasswordResetTokenForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        String token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiry(Instant.now().plus(15, ChronoUnit.MINUTES)); // Token platný 15 minut
        userRepository.save(user);
        return token;
    }

    /**
     * Resetování hesla uživatele pomocí tokenu.
     *
     * @param token       Token pro reset hesla.
     * @param newPassword Nové heslo.
     * @throws IllegalArgumentException pokud je token neplatný nebo vypršel.
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid password reset token"));

        if (user.getPasswordResetTokenExpiry().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Password reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);
    }

    /**
     * Validace unikátnosti uživatelského jména a emailu při registraci.
     *
     * @param user Uživatel k validaci.
     * @throws IllegalArgumentException pokud uživatelské jméno nebo email již
     *                                  existuje.
     */
    private void validateUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            log.warn("Registrace selhala - jméno {} již existuje", user.getUsername());
            throw new IllegalArgumentException("Uživatelské jméno již existuje");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            log.warn("Registrace selhala - email {} již existuje", user.getEmail());
            throw new IllegalArgumentException("Email již existuje");
        }
    }

    // getters, setters, další metody...
    @Transactional
    @CacheEvict(value = { "users", "usersById", "allUsers" }, allEntries = true)
    public void DeleteUserById(Long userId) {
        userRepository.deleteById(userId);
    }

    @Cacheable(value = "users", key = "#username")
    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * Změna role uživatele.
     *
     * @param userId  ID uživatele.
     * @param newRole Nová role (USER nebo ADMIN).
     */
    @Transactional
    @CacheEvict(value = { "users", "usersById", "allUsers" }, allEntries = true)
    public void changeUserRole(Long userId, User.Role newRole) {
        User user = findUserById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setRole(String.valueOf(newRole));
        userRepository.save(user);
    }

    @Cacheable(value = "usersById", key = "#userId")
    public Optional<User> findUserById(Long userId) {
        return userRepository.findById(userId);
    }

    @Cacheable("allUsers")
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public boolean isOwner(Long userId, String username) {
        return userRepository.findById(userId)
                .map(user -> user.getUsername().equals(username))
                .orElse(false);
    }
}

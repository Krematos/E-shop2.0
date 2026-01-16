package org.example.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.UserUpdateResponse;
import org.example.event.UserRegisteredEvent;
import org.example.model.User;
import org.example.model.enums.Role;
import org.example.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final ApplicationEventPublisher eventPublisher;

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
        // 1. Validace
        validateUser(user);
        // 2. Encode hesla
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 3. Nastavení defaultní role
        user.setRoles(new HashSet<>(Collections.singletonList(Role.USER)));
        // 4. Uložení do DB
        User savedUser = userRepository.save(user);
        log.info("Uživatel uložen do DB, publikuji event...");
        // 5. Odeslání emailu
        eventPublisher.publishEvent(new UserRegisteredEvent(savedUser));

        return savedUser;
    }

    @Transactional
    @CacheEvict(value = { "users", "usersById", "allUsers" }, allEntries = true)
    public User updateUser(User user, UserUpdateResponse userUpdateResponse) {

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
    public void changeUserRole(Long userId, Role newRole) {
        // 1. Načtení uživatele
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Uživatel s ID " + userId + " nebyl nalezen"));

        // 2. Aktualizace rolí
        user.setRoles(new HashSet<>(Collections.singletonList(newRole)));

        // Varianta B: Pokud by mohl mít více rolí a chcete jen přidat/odebrat
         user.getRoles().clear();
         user.addRole(newRole);

        // 3. Save není nutný díky @Transactional (Dirty Checking), ale pro čitelnost neškodí
        userRepository.save(user);

        log.info("Role uživatele {} změněna na {}", user.getUsername(), newRole);
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

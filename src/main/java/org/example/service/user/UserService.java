package org.example.service.user;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.example.service.email.EmailService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.time.LocalDateTime;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;


    @Transactional
    @CacheEvict(value = {"users", "usersById", "allUsers"}, allEntries = true)
    public User registerNewUser(User user){
        validateUser(user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Set.of(User.Role.ROLE_USER)); // Výchozí role

        User savedUser = userRepository.save(user);

        log.info("Nový uživatel registrován: {}", savedUser.getUsername());

        // Odeslání uvítacího emailu
        new Thread(() -> emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getUsername()));

        return savedUser;
    }

    public String createPasswordResetTokenForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        String token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1)); // Token platný 1 hodinu
        userRepository.save(user);
        return token;
    }

    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid password reset token"));

        if (user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Password reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);
    }

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
    @CacheEvict(value = {"users", "usersById", "allUsers"}, allEntries = true)
    public void DeleteUserById(Long userId) {
        userRepository.deleteById(userId);
    }

    @Cacheable(value = "users", key = "#username")
    public Optional<User> findUserByUsername(String username){
        return userRepository.findByUsername(username);
    }

    public User save(User user){
        return userRepository.save(user);
    }

    @Cacheable(value = "usersById", key = "#userId")
    public Optional<User> findUserById(Long userId) {
        return userRepository.findById(userId);
    }
    @Cacheable("allUsers")
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }



}

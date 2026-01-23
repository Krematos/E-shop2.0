package krematos.service;


import krematos.model.User;
import krematos.repository.UserRepository;
import krematos.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import krematos.model.PasswordResetToken;
import krematos.repository.PasswordResetRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class PasswordResetService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordResetRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    /**  Uživatel požádá o reset hesla
     *
     * @param email Email uživatele, který chce resetovat heslo.
     */
    public void initiatePasswordReset(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            // I když email neexistuje, navenek se tváří, že je vše OK.
            // Zabrání se  tím tzv. "User Enumeration Attack" (hacker nemůže zkoušet emaily).
            log.warn("Požadavek na reset hesla pro neexistující email: {}", email);
            return;
        }

        User user = userOptional.get();

        // Vygeneruje náhodný token
        String token = UUID.randomUUID().toString();

        // Uloží do DB
        PasswordResetToken myToken = new PasswordResetToken(token, user, null);
        tokenRepository.save(myToken);

        // Pošle email (asynchronně, aby uživatel nečekal)
        emailService.sendPasswordResetEmail(user.getEmail(), token);
    }

    /** Uživatel resetuje heslo pomocí tokenu
     *
     * @param token Token pro reset hesla.
     * @param newPassword Nové heslo.
     * @throws IllegalArgumentException pokud je token neplatný nebo vypršel.
     */
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Neplatný token"));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken); // Úklid
            throw new IllegalArgumentException("Token vypršel");
        }

        User user = resetToken.getUser();

        // Změna hesla + Hashování
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Token po použití okamžitě smazat!
        tokenRepository.delete(resetToken);
    }




}

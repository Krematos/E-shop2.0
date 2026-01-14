package org.example.repository;

import org.example.model.User;
import org.example.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetRepository extends JpaRepository<PasswordResetToken, Long> {
    // 1. Metoda pro hledání tokenu
    Optional<PasswordResetToken> findByToken(String token);
    // 2. Metoda pro smazání všech tokenů daného uživatele
    void deleteByUser(User user);
    // 3. Metoda pro Cron Job (automatický úklid)
    void deleteByExpiryDateBefore(Instant now);
}

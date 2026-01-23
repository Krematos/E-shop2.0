package krematos.repository;

import krematos.model.PasswordResetToken;
import krematos.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface PasswordResetRepository extends JpaRepository<PasswordResetToken, Long> {
    // 1. Metoda pro hledání tokenu
    Optional<PasswordResetToken> findByToken(String token);
    // 2. Metoda pro smazání všech tokenů daného uživatele
    void deleteByUser(User user);
    // 3. Metoda pro Cron Job (automatický úklid)
    void deleteByExpiryDateBefore(Instant now);
}

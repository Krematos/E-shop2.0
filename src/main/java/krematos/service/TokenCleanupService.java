package krematos.service;

import lombok.extern.slf4j.Slf4j;
import krematos.repository.PasswordResetRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
public class TokenCleanupService {
    private final PasswordResetRepository passwordResetRepository;

    public TokenCleanupService(PasswordResetRepository passwordResetRepository) {
        this.passwordResetRepository = passwordResetRepository;
    }

    // Spustí se každý den ve 2:00 ráno
    // cron formát: sec min hour day month day-of-week
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional // Důležité pro mazací operace
    public void removeExpiredTokens() {
        Instant now = Instant.now();
        passwordResetRepository.deleteByExpiryDateBefore(now);
        log.info("Vypršené tokeny pro reset hesla byly úspěšně odstraněny.");
    }
}

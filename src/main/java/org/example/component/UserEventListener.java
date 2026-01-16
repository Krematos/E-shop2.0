package org.example.component;

import org.example.event.UserRegisteredEvent;
import org.example.service.email.EmailService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class UserEventListener {

    private EmailService emailService;
    @Async // Běží v jiném vlákně
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // Až když je user bezpečně v DB
    public void handleUserRegistered(UserRegisteredEvent event) {
        emailService.sendWelcomeEmail(
                event.user().getEmail(),
                event.user().getUsername()
        );
    }
}

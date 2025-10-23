package org.example.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = new TemplateEngine();
    }

    /**
     * Odeslání e-mailu asynchronně (nebude blokovat hlavní vlákno)
     */
    @Async
    public void sendWelcomeEmail(String to, String username) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

            // Připravíme HTML šablonu s proměnnou username
            Context context = new Context();
            context.setVariable("username", username);
            String htmlContent = templateEngine.process("welcome-email", context);

            helper.setTo(to);
            helper.setSubject("Vítejte v SecondEL!");
            helper.setText(htmlContent, true);
            helper.setFrom("noreply@secondel.cz");

            mailSender.send(message);
            System.out.println("✅ Odeslán uvítací e-mail na " + to);
        } catch (Exception e) {
            System.err.println("❌ Chyba při odesílání e-mailu: " + e.getMessage());
        }
    }
}

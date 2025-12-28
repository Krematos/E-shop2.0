package org.example.service.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;

@Slf4j
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
        log.info("Volána metoda pro odeslání uvítacího e-mailu");
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
            log.error("Chyba při odesílání uvítacího e-mailu: {}", e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetEmail(String userEmail, String resetToken) {
        log.info("Volána metoda pro odeslání e-mailu pro reset hesla");
        // Implementace odeslání e-mailu pro reset hesla
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
            String resetUrl = "https://www.tvoje-domena.cz/reset-hesla?token=" + resetToken;

            // Vytvoření HTML obsahu e-mailu
            String htmlContent = "<html>"
                    + "<body>"
                    + "<h2>Obnovení hesla</h2>"
                    + "<p>Obdrželi jsme žádost o obnovení vašeho hesla. Pro nastavení nového hesla klikněte na níže uvedený odkaz:</p>"
                    + "<a href=\"" + resetUrl + "\" style=\"background-color:#007bff; color:white; padding:10px 15px; text-decoration:none; border-radius:5px;\">Obnovit heslo</a>"
                    + "<p>Pokud jste o reset hesla nežádali, tento e-mail prosím ignorujte.</p>"
                    + "<br>"
                    + "<p>Děkujeme,<br>Tým Tvé Aplikace</p>"
                    + "</body>"
                    + "</html>";

            // Nastavení HTML obsahu (druhý argument 'true' říká, že text je HTML)
            helper.setText(htmlContent, true);

            // Odeslání e-mailu
            mailSender.send(message);
            helper.setTo(userEmail);
            helper.setSubject("Žádost o obnovení hesla");
        } catch (Exception e) {
            log.error("Chyba při odesílání e-mailu pro reset hesla: {}", e.getMessage());
        }
    }
}

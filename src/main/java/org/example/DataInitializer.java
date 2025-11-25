package org.example;

import lombok.extern.slf4j.Slf4j;
import org.example.model.Product;
import org.example.model.User;
import org.example.model.User.Role;
import org.example.repository.UserRepository;
import org.mapstruct.control.MappingControl;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;
@Slf4j
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminUsername = "HlavníAdmin";
            if (userRepository.findByUsername(adminUsername).isEmpty()) {
                // Vytvoříme nového administrátora
                User admin = User.builder()
                        .username(adminUsername)
                        .email("farm140warrior@gmail.com")
                        .password(passwordEncoder.encode("SecureP@ssw0rd"))
                        .roles(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER))
                        .build();
                // Uložíme administrátora do databáze
                userRepository.save(admin);
                log.info("Vytvořen nový admin: {}", adminUsername);
            } else {
                log.info("Admin již existuje: {}", adminUsername);
            }
        };
    }

    /*@Bean
    CommandLineRunner initProducts() {
        return args -> {

            // Místo pro inicializaci produktů v databázi, pokud je potřeba
            Product product = Product.builder()
                    .name("Ukázkový produkt")
                    .description("Toto je popis ukázkového produktu.")
                    .price(new java.math.BigDecimal("19.99"))
                    .category("Ukázková kategorie")
                    .currency("CZK")
                    .build();
        };
    }*/
}

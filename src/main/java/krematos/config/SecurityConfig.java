package krematos.config;

import krematos.service.impl.UserDetailsServiceImpl;
import lombok.AllArgsConstructor;
import krematos.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.List;

import static org.apache.tomcat.util.http.Method.DELETE;
import static org.apache.tomcat.util.http.Method.PUT;

@AllArgsConstructor
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    //umožní definovat frontend URL v application.properties
    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:5174}")
    private String[] allowedOrigins;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Aktivuje CORS konfiguraci definovanou níže
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll() // Swagger API dokumentace
                        .requestMatchers("/api/auth/**").permitAll() // registrace, login
                        .requestMatchers("/uploads/**").permitAll() // přístup k obrázkům bez přihlášení
                        .requestMatchers("GET", "/api/products").permitAll() // Zobrazení produktů bez přihlášení
                        .requestMatchers("GET", "/api/products/**").permitAll() // Detail produktu bez přihlášení
                        .requestMatchers("GET", "/api/categories").permitAll() // Zobrazení kategorií bez přihlášení
                        .requestMatchers("GET", "/api/images/**").permitAll() // Zobrazení obrázků bez přihlášení

                        .requestMatchers(HttpMethod.POST, PUT, DELETE, "/api/products/admin/**").hasRole("ADMIN")// Vytváření, úprava a mazání produktů pouze pro ADMin
                        .requestMatchers("/api/auth/forgot-password", "/api/auth/reset-password").permitAll() // Povolit přístup k resetu hesla
                        .requestMatchers("/error/**").permitAll() // Povolit přístup k chybovým stránkám
                        .requestMatchers("GET", "/api/analytics/**").hasRole("ADMIN") // Přístup k analytice pouze pro ADMIN
                        .requestMatchers("POST", "/api/analytics/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // 2. Definice pravidel pro CORS
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Použije pole načtené přes @Value
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type", "Set-Cookie"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

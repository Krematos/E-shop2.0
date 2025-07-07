package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


import org.example.service.UserService;
import org.example.security.JwtAuthenticationFilter;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserService userService;



    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, UserService UserService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userService = UserService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserService userService) {
        return username -> userService.findUserByUsername(username)
                .map(user -> org.springframework.security.core.userdetails.User.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .roles(user.getRoles().stream().map(Enum::name).toArray(String[]::new))
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Uživatel nenalezen: " + username));
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService(userService));
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // Zakáže CSRF ochranu pro jednoduchost, v produkci by měla být povolena
            .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers("/api/auth/**").permitAll() // Veřejné endpointy pro autentizaci
                    .requestMatchers(HttpMethod.GET, "/api/products/**").authenticated() // Ochrání endpointy pro produkty
                    .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN") // Pouze admin může přidávat produkty
                    .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN") // Pouze admin může aktualizovat produkty
                    .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN") // Pouze admin může mazat produkty
                    .requestMatchers("/api/orders/**").hasAnyRole("USER", "ADMIN")// Uživatelé: zobrazení detailů pouze pro ADMINa nebo samotného uživatele
                    .requestMatchers("/api/users/**").hasAnyRole("USER","ADMIN") // Pouze admin může spravovat uživatele
                    .anyRequest().authenticated() // Všechny ostatní požadavky musí být autentizovány
            )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Použije stateless session management
            .authenticationProvider(authenticationProvider()) // Nastaví vlastní autentizační poskytovatele
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // Přidá JWT autentizační filtr
        return http.build();
    }
}



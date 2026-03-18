package com.um.labtrack.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration.
 * Configures authentication, authorization, and password encoding.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Password encoder bean using BCrypt.
     *
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Security filter chain configuration.
     * Allows public access to setup and login endpoints.
     * All other endpoints are handled by custom AuthService checks in controllers.
     * This allows our custom authentication system to work without Spring Security blocking.
     *
     * @param http HttpSecurity builder
     * @return SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for REST API
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/setup/**").permitAll()
                .requestMatchers("/api/auth/login").permitAll()
                // Debug endpoint (for testing - should be restricted in production)
                .requestMatchers("/api/setup/debug/**").permitAll()
                // All other endpoints are permitted - authentication is handled by custom AuthService in controllers
                // This allows ADMIN and other roles to access all endpoints, with authorization checked in service layer
                .anyRequest().permitAll()
            );

        return http.build();
    }
}

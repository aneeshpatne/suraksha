package com.aneesh.suraksha;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for Postman/testing
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/users").permitAll() // Allow access to /login and /users
                        .anyRequest().authenticated() // Require auth for everything else
                );

        return http.build();
    }
}

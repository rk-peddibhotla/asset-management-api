
package com.ravikanth.asset_management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
public class SecurityConfig {

    // Configure the application's security rules
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                // JWT authentication does not use browser sessions
                .csrf(csrf -> csrf.disable())

                // Allow requests from the frontend
                .cors(cors -> cors.configurationSource(
                        corsConfigurationSource()
                ))

                // Make the API stateless
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // Define which endpoints require authentication
                .authorizeHttpRequests(auth -> auth

                        // Registration and login are public
                        .requestMatchers("/api/auth/**")
                        .permitAll()

                        // Admin endpoints require the ADMIN role
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        // Only admins can create categories
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/categories"
                        )
                        .hasRole("ADMIN")

                        // Only admins can create assets
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/assets"
                        )
                        .hasRole("ADMIN")

                        // All other endpoints require authentication
                        .anyRequest()
                        .authenticated()
                )

                // Validate JWT tokens on protected requests
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(
                                        jwtAuthenticationConverter()
                                )
                        )
                );

        return http.build();
    }

    // Configure which frontend can call the API
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of("http://localhost:5173")
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PATCH",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of("*")
        );

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    // Convert the JWT role claim into a Spring Security authority
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        // Read the user's role from the JWT "role" claim
        authoritiesConverter.setAuthoritiesClaimName("role");

        // Convert ADMIN into ROLE_ADMIN and STUDENT into ROLE_STUDENT
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
                authoritiesConverter
        );

        return converter;
    }
}


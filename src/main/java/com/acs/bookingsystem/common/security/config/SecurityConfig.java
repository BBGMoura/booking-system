package com.acs.bookingsystem.common.security.config;

import com.acs.bookingsystem.common.security.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable) // Disable CSRF protection for stateless APIs -> not needed if using JWT token
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/auth/**",
                                     "/password/**",
                                     "/swagger-ui/**",
                                     "/v3/api-docs/**",
                                     "/swagger-ui.html",
                                     "/h2-console/**") // Public endpoints
                    .permitAll()
                    .requestMatchers("/admin/**") // Admin-specific endpoints
                    .hasAuthority("ADMIN")
                    .anyRequest() // All other requests
                    .authenticated() // Must be authenticated
            )
            .headers(headers -> headers
                    .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable) // Allow iframe embedding for H2 console (disables X-Frame-Options header allowing iframe embedding)
            )
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Use stateless sessions -> no HTTP session is created or used by Spring Security. This is common if using JWT tokens, as they are stateless and don't require server-side storage.
            )
            .authenticationProvider(authenticationProvider) // Custom authentication provider
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); // JWT filter before default filter
        return http.build();
    }
}

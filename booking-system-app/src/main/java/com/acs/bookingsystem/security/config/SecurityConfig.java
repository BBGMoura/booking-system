package com.acs.bookingsystem.security.config;

import com.acs.bookingsystem.security.filter.JwtAuthenticationFilter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthFilter;

  @Value("${allowed.origins}")
  private String allowedOrigins;

  @Bean
  @SuppressWarnings("java:S4502") // CSRF safe to disable — stateless JWT API, no session cookies
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(
            AbstractHttpConfigurer
                ::disable) // Disable CSRF protection for stateless APIs -> not needed if using JWT
        // token
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/api/*/auth/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/h2-console/**")
                    .permitAll()
                    .requestMatchers("/api/*/admin/**")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .authenticated())
        .headers(
            // Allow iframe embedding for H2 console (disables X-Frame-Options header allowing
            // iframe embedding)
            headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
        .sessionManagement(
            // Use stateless sessions -> no HTTP session is created or used by Spring Security.
            // This is common if using JWT tokens, as they are stateless and don't require
            // server-side storage.
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(
            jwtAuthFilter,
            UsernamePasswordAuthenticationFilter.class); // JWT filter before default filter
    return http.build();
  }

  @Bean
  static RoleHierarchy roleHierarchy() {
    return RoleHierarchyImpl.withDefaultRolePrefix().role("ADMIN").implies("USER").build();
  }

  // enable pre-post method security with role hierarchy
  @Bean
  static MethodSecurityExpressionHandler methodSecurityExpressionHandler(
      RoleHierarchy roleHierarchy) {
    DefaultMethodSecurityExpressionHandler expressionHandler =
        new DefaultMethodSecurityExpressionHandler();
    expressionHandler.setRoleHierarchy(roleHierarchy);
    return expressionHandler;
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(
        List.of(allowedOrigins)); // Only allow requests from our frontend URL (CORS is only for
    // browsers, will not block cURLs)
    configuration.setAllowedMethods(
        List.of(
            "GET", "POST", "PATCH", "PUT", "DELETE",
            "OPTIONS")); // OPTIONS is required for preflight requests
    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    configuration.setAllowCredentials(
        true); // Allow cookies and auth headers to be sent cross-origin

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}

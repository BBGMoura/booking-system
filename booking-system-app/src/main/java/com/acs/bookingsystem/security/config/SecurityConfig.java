package com.acs.bookingsystem.security.config;

import com.acs.bookingsystem.security.filter.JwtAuthenticationFilter;
import java.util.Arrays;
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
    return buildCorsConfigurationSource(allowedOrigins);
  }

  /**
   * @param allowedOrigins comma-separated origins; entries may contain {@code *} wildcards
   * @throws IllegalStateException if no origin is configured or an entry is a bare {@code *}
   */
  static CorsConfigurationSource buildCorsConfigurationSource(String allowedOrigins) {
    CorsConfiguration configuration = new CorsConfiguration();
    // Patterns (not plain origins) are required for wildcards alongside allowCredentials(true).
    // CORS is only for browsers, it will not block cURLs.
    configuration.setAllowedOriginPatterns(parseAllowedOrigins(allowedOrigins));
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

  static List<String> parseAllowedOrigins(String allowedOrigins) {
    List<String> origins =
        Arrays.stream(allowedOrigins == null ? new String[0] : allowedOrigins.split(","))
            .map(String::trim)
            .filter(origin -> !origin.isEmpty())
            .toList();
    if (origins.isEmpty()) {
      throw new IllegalStateException("allowed.origins must contain at least one origin");
    }
    if (origins.contains("*")) {
      // A bare wildcard combined with credentials would let any site make authenticated requests.
      throw new IllegalStateException(
          "allowed.origins must not contain a bare '*'; list explicit origins or patterns such as"
              + " 'https://app-pr-*.example.com'");
    }
    return origins;
  }
}

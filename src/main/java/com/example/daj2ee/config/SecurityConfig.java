package com.example.daj2ee.config;

import com.example.daj2ee.security.JwtAuthenticationFilter;
import java.util.Arrays;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Security configuration for the application.
 *
 * - Stateless JWT-based authentication using {@link JwtAuthenticationFilter}.
 * - Public endpoints:
 *   - /api/auth/** (authentication)
 *   - /api/problems/** (public listing of problems)
 *   - /h2-console/** (H2 database console - allowed for development)
 *   - OpenAPI/Swagger endpoints
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Autowired
  public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(
    org.springframework.security.config.annotation.web.builders.HttpSecurity http
  ) throws Exception {
    http
      // No CSRF as we use stateless JWTs for API protection
      .csrf(csrf -> csrf.disable())
      // Enable CORS (uses the CorsConfigurationSource bean below)
      .cors(Customizer.withDefaults())
      // Stateless session (no HTTP session)
      .sessionManagement(session ->
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      )
      // Authorization rules
      .authorizeHttpRequests(auth ->
        auth
          .requestMatchers(
            "/api/auth/**",
            "/api/problems/**",
            "/api/comments/problems/**",
            "/h2-console/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/health"
          )
          .permitAll()
          .anyRequest()
          .authenticated()
      )
      // Add JWT filter before username/password auth filter
      .addFilterBefore(
        jwtAuthenticationFilter,
        UsernamePasswordAuthenticationFilter.class
      );

    // Allow H2 console frames (for development)
    http.headers(headers ->
      headers.frameOptions(frameOptions -> frameOptions.sameOrigin())
    );

    return http.build();
  }

  /**
   * Expose the authentication manager used by Spring Security so we can use it in controllers/services.
   */
  @Bean
  public AuthenticationManager authenticationManager(
    AuthenticationConfiguration authenticationConfiguration
  ) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  /**
   * BCrypt password encoder bean.
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  // WebSecurityCustomizer bean removed for compatibility with current Spring Security version.

  /**
   * Basic CORS configuration that allows common HTTP methods from any origin.
   * Adjust for production usage (restrict origins and credentials as appropriate).
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
    configuration.setAllowedMethods(
      Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")
    );
    configuration.setAllowedHeaders(Collections.singletonList("*"));
    configuration.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source =
      new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}

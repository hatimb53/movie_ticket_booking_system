package com.mtbs.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtbs.auth.JwtAuthenticationFilter;
import com.mtbs.common.error.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Stateless JWT security. Auth endpoints and the H2 console are public; everything else requires a
 * valid token, with method-level {@code @PreAuthorize} enforcing roles. 401/403 responses reuse the
 * global {@link ErrorResponse} shape for a consistent error contract.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final ObjectMapper objectMapper;

  public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, ObjectMapper objectMapper) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.objectMapper = objectMapper;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/**", "/h2-console/**").permitAll()
            .anyRequest().authenticated())
        .headers(headers -> headers.frameOptions(frame -> frame.disable())) // H2 console
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(unauthorizedEntryPoint())
            .accessDeniedHandler(forbiddenHandler()))
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  private AuthenticationEntryPoint unauthorizedEntryPoint() {
    return (request, response, authException) ->
        writeError(response, HttpStatus.UNAUTHORIZED, "Authentication required", request.getRequestURI());
  }

  private AccessDeniedHandler forbiddenHandler() {
    return (request, response, accessDeniedException) ->
        writeError(response, HttpStatus.FORBIDDEN, "Access denied", request.getRequestURI());
  }

  private void writeError(HttpServletResponse response, HttpStatus status, String message, String path)
      throws java.io.IOException {
    ErrorResponse body = new ErrorResponse(
        Instant.now(), status.value(), status.getReasonPhrase(), message, path, null);
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getWriter(), body);
  }
}

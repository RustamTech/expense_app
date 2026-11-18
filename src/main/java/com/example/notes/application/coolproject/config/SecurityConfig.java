package com.example.notes.application.coolproject.config;

import com.example.notes.application.coolproject.security.InvalidTokenException;
import com.example.notes.application.coolproject.security.JwtAuthFilter;
import com.example.notes.application.coolproject.security.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Stateless JWT security.
 *
 * <p>401 and 403 are not written here: both are delegated to the MVC {@link
 * HandlerExceptionResolver}, so {@code GlobalExceptionHandler} renders them in the same
 * ProblemDetail format as every other error.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthFilter jwtAuthFilter;
  private final UserDetailsServiceImpl userDetailsService;

  /**
   * Builds the security filter chain.
   *
   * @param http security builder
   * @param exceptionResolver MVC resolver used to render 401/403 responses
   * @return the configured filter chain
   * @throws Exception if the builder fails
   */
  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver)
      throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/api/notes/auth/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/swagger-ui/index.html",
                        "/v3/api-docs/**",
                        "/api-docs/**",
                        "/api-docs/swagger-config",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/error")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(
                        (request, response, authException) ->
                            exceptionResolver.resolveException(
                                request, response, null, refine(request, authException)))
                    .accessDeniedHandler(
                        (request, response, deniedException) ->
                            exceptionResolver.resolveException(
                                request, response, null, deniedException)))
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /** Reports a rejected bearer token as such, instead of a generic "authentication required". */
  private static AuthenticationException refine(
      HttpServletRequest request, AuthenticationException original) {
    return request.getAttribute(JwtAuthFilter.INVALID_TOKEN_ATTRIBUTE) != null
        ? new InvalidTokenException("Bearer token rejected")
        : original;
  }
}

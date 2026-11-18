package com.example.notes.application.coolproject.config;

import com.example.notes.application.coolproject.enums.UserRole;
import com.example.notes.application.coolproject.security.InvalidTokenException;
import com.example.notes.application.coolproject.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
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
 * Stateless JWT security with two roles.
 *
 * <table>
 *   <caption>Access rules</caption>
 *   <tr><th>Path</th><th>Access</th></tr>
 *   <tr><td>{@code /auth/**}, Swagger, {@code /error}</td><td>everyone</td></tr>
 *   <tr><td>{@code /admin/**}</td><td>{@code ADMIN}</td></tr>
 *   <tr><td>{@code POST/PUT/DELETE /categories/**}</td><td>{@code ADMIN}</td></tr>
 *   <tr><td>everything else</td><td>any authenticated user</td></tr>
 * </table>
 *
 * <p>401 and 403 are not written here: both are delegated to the MVC {@link
 * HandlerExceptionResolver}, so {@code GlobalExceptionHandler} renders them in the same
 * ProblemDetail format as every other error.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private static final String ADMIN = UserRole.ADMIN.name();
  private static final String CATEGORY_PATHS = Endpoints.CATEGORIES + Endpoints.ALL_SUBPATHS;

  /**
   * Builds the security filter chain.
   *
   * @param http security builder
   * @param jwtAuthFilter filter that authenticates bearer tokens
   * @param exceptionResolver MVC resolver used to render 401/403 responses
   * @return the configured filter chain
   * @throws Exception if the builder fails
   */
  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      JwtAuthFilter jwtAuthFilter,
      @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver)
      throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(Endpoints.PUBLIC)
                    .permitAll()
                    .requestMatchers(Endpoints.ADMIN + Endpoints.ALL_SUBPATHS)
                    .hasRole(ADMIN)
                    .requestMatchers(HttpMethod.POST, Endpoints.CATEGORIES, CATEGORY_PATHS)
                    .hasRole(ADMIN)
                    .requestMatchers(HttpMethod.PUT, CATEGORY_PATHS)
                    .hasRole(ADMIN)
                    .requestMatchers(HttpMethod.DELETE, CATEGORY_PATHS)
                    .hasRole(ADMIN)
                    .anyRequest()
                    .authenticated())
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
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  /**
   * Exposes the authentication manager auto-configured from the {@code UserDetailsService} and
   * {@link PasswordEncoder} beans; used by the login flow.
   *
   * @param config Spring Security authentication configuration
   * @return the authentication manager
   * @throws Exception if the manager cannot be built
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  /**
   * Password hashing used for registration and login.
   *
   * @return a BCrypt encoder
   */
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

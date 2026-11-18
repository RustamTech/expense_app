package com.example.notes.application.coolproject.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Authenticates requests that carry an {@code Authorization: Bearer <jwt>} header.
 *
 * <p>The filter never rejects a request itself. A missing token leaves the request anonymous; an
 * invalid token additionally sets {@link #INVALID_TOKEN_ATTRIBUTE}, so the entry point can answer
 * 401 {@code INVALID_TOKEN} instead of a generic {@code AUTHENTICATION_REQUIRED}. Tokens of
 * disabled or deleted users are not accepted, so blocking a user takes effect immediately.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

  /** Request attribute set when a bearer token was present but could not be accepted. */
  public static final String INVALID_TOKEN_ATTRIBUTE = JwtAuthFilter.class.getName() + ".invalid";

  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtService jwtService;
  private final UserDetailsServiceImpl userDetailsService;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    boolean alreadyAuthenticated = SecurityContextHolder.getContext().getAuthentication() != null;
    if (header != null && header.startsWith(BEARER_PREFIX) && !alreadyAuthenticated) {
      authenticate(request, header.substring(BEARER_PREFIX.length()));
    }
    filterChain.doFilter(request, response);
  }

  private void authenticate(HttpServletRequest request, String token) {
    try {
      UserDetails user = userDetailsService.loadUserByUsername(jwtService.extractEmail(token));
      if (!jwtService.isTokenValid(token, user)) {
        markInvalid(request, "token does not match an enabled account");
        return;
      }
      var authentication =
          new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authentication);
    } catch (JwtException | IllegalArgumentException | UsernameNotFoundException e) {
      markInvalid(request, e.getClass().getSimpleName());
    }
  }

  private static void markInvalid(HttpServletRequest request, String reason) {
    log.debug("Rejected bearer token: {}", reason);
    request.setAttribute(INVALID_TOKEN_ATTRIBUTE, Boolean.TRUE);
  }
}

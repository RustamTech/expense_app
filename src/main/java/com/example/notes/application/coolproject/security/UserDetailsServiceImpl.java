package com.example.notes.application.coolproject.security;

import com.example.notes.application.coolproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads users for Spring Security by email. Used both by the login flow (through the
 * auto-configured {@code DaoAuthenticationProvider}) and by {@link JwtAuthFilter}.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRepository userRepository;

  /**
   * {@inheritDoc}
   *
   * @param email email of the user, case-insensitive
   * @throws UsernameNotFoundException if no user has this email
   */
  @Override
  public UserDetails loadUserByUsername(String email) {
    return userRepository
        .findByEmail(EmailNormalizer.normalize(email))
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
  }
}

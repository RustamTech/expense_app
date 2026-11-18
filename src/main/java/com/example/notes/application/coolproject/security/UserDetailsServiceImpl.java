package com.example.notes.application.coolproject.security;

import com.example.notes.application.coolproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    log.info("Loading user by email: {}", email);
    return userRepository
        .findByEmail(email)
        .orElseThrow(
            () -> {
              log.error("User not found with this email: {}", email);
              return new UsernameNotFoundException("User not found with this email: " + email);
            });
  }
}

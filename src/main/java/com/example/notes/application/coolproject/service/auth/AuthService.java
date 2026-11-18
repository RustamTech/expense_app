package com.example.notes.application.coolproject.service.auth;

import com.example.notes.application.coolproject.dto.auth.AuthResponse;
import com.example.notes.application.coolproject.dto.auth.LoginRequest;
import com.example.notes.application.coolproject.dto.auth.RegisterRequest;
import com.example.notes.application.coolproject.exception.custom.ResourceAlreadyExistsException;
import org.springframework.security.core.AuthenticationException;

/** Registration and login. */
public interface AuthService {

  /**
   * Registers a new account with the {@code USER} role and issues a token for it.
   *
   * @param request registration payload; the email is normalized before it is stored
   * @return the token and basic profile
   * @throws ResourceAlreadyExistsException if the email is already registered
   */
  AuthResponse register(RegisterRequest request);

  /**
   * Verifies credentials and issues a token.
   *
   * @param request login payload; the email is case-insensitive
   * @return the token and basic profile
   * @throws AuthenticationException if the credentials are wrong or the account is disabled
   */
  AuthResponse login(LoginRequest request);
}

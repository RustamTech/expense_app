package com.example.notes.application.coolproject.security;

import org.springframework.security.core.AuthenticationException;

/** Signals that a bearer token was sent but is malformed, badly signed, expired or revoked. */
public class InvalidTokenException extends AuthenticationException {

  /**
   * Creates the exception.
   *
   * @param message internal description, never returned to the client
   */
  public InvalidTokenException(String message) {
    super(message);
  }
}

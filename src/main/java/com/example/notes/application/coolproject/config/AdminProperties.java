package com.example.notes.application.coolproject.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Credentials of the bootstrap administrator, bound from {@code app.admin.*} (env {@code
 * ADMIN_EMAIL}, {@code ADMIN_PASSWORD}). When either value is blank no administrator is created.
 *
 * @param email email of the administrator account
 * @param password initial raw password; hashed before it is stored
 */
@ConfigurationProperties(prefix = "app.admin")
public record AdminProperties(String email, String password) {

  /**
   * Tells whether bootstrap credentials were supplied.
   *
   * @return {@code true} if both email and password are non-blank
   */
  public boolean isConfigured() {
    return email != null && !email.isBlank() && password != null && !password.isBlank();
  }
}

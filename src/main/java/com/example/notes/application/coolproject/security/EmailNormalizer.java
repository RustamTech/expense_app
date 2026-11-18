package com.example.notes.application.coolproject.security;

import java.util.Locale;

/** Normalizes emails so {@code John@Mail.com} and {@code john@mail.com} are the same account. */
public final class EmailNormalizer {

  private EmailNormalizer() {}

  /**
   * Trims and lower-cases an email.
   *
   * @param email raw email, may be {@code null}
   * @return normalized email, or {@code null} if the input is {@code null}
   */
  public static String normalize(String email) {
    return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
  }
}

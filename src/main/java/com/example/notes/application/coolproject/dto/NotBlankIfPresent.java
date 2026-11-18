package com.example.notes.application.coolproject.dto;

/**
 * Validation rule for optional text fields of partial updates: {@code null} means "keep the current
 * value", but a present value must contain at least one non-whitespace character. Used with
 * {@code @Pattern}, which accepts {@code null}.
 */
public final class NotBlankIfPresent {

  /** Matches any text with at least one non-whitespace character. */
  public static final String PATTERN = "(?s).*\\S.*";

  /** Validation message for {@link #PATTERN}. */
  public static final String MESSAGE = "must not be blank";

  private NotBlankIfPresent() {}
}

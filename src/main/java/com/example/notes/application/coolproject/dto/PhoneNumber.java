package com.example.notes.application.coolproject.dto;

/** Shared validation rule for phone numbers in request DTOs. */
public final class PhoneNumber {

  /** Optional leading {@code +} followed by 7–15 digits (E.164 length). */
  public static final String PATTERN = "^\\+?[0-9]{7,15}$";

  /** Validation message for {@link #PATTERN}. */
  public static final String MESSAGE = "must be 7-15 digits, optionally starting with +";

  private PhoneNumber() {}
}

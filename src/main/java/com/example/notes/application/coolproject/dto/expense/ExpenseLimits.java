package com.example.notes.application.coolproject.dto.expense;

/** Validation limits for expense payloads, aligned with the {@code expenses} table columns. */
public final class ExpenseLimits {

  /** Digits before the decimal point; column is {@code numeric(19,2)}. */
  public static final int AMOUNT_INTEGER_DIGITS = 17;

  /** Digits after the decimal point. */
  public static final int AMOUNT_FRACTION_DIGITS = 2;

  /** Maximum description length. */
  public static final int DESCRIPTION_MAX = 255;

  /** Maximum note length. */
  public static final int NOTE_MAX = 1000;

  private ExpenseLimits() {}
}

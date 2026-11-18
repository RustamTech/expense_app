package com.example.notes.application.coolproject.exception;

import lombok.Getter;

/**
 * Root of all business exceptions. The HTTP status is derived from the {@link ErrorCode}, and the
 * message is safe to show to the client.
 */
@Getter
public abstract class BaseException extends RuntimeException {

  private final ErrorCode errorCode;

  /**
   * Creates a business exception.
   *
   * @param errorCode machine-readable code that also defines the HTTP status
   * @param message client-safe description of the problem
   */
  protected BaseException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }
}

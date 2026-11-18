package com.example.notes.application.coolproject.exception;

import org.springframework.http.HttpStatus;

/**
 * Machine-readable error codes returned in the {@code code} property of every error response.
 *
 * <p>Each code is bound to the HTTP status it is answered with, so the status is defined in one
 * place and clients can branch on {@code code} instead of parsing messages.
 */
public enum ErrorCode {
  // 400
  VALIDATION_FAILED(HttpStatus.BAD_REQUEST),
  MALFORMED_JSON(HttpStatus.BAD_REQUEST),
  INVALID_PARAMETER(HttpStatus.BAD_REQUEST),
  MISSING_PARAMETER(HttpStatus.BAD_REQUEST),
  BAD_REQUEST(HttpStatus.BAD_REQUEST),

  // 401
  AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED),
  INVALID_TOKEN(HttpStatus.UNAUTHORIZED),
  INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED),

  // 403
  ACCESS_DENIED(HttpStatus.FORBIDDEN),

  // 404
  RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND),
  EXPENSE_NOT_FOUND(HttpStatus.NOT_FOUND),
  CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND),
  USER_NOT_FOUND(HttpStatus.NOT_FOUND),

  // 405 / 406 / 415
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED),
  NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE),
  UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE),

  // 409
  RESOURCE_ALREADY_EXISTS(HttpStatus.CONFLICT),
  CATEGORY_IN_USE(HttpStatus.CONFLICT),
  OPERATION_NOT_ALLOWED(HttpStatus.CONFLICT),
  DATA_CONFLICT(HttpStatus.CONFLICT),

  // 500
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

  private final HttpStatus status;

  ErrorCode(HttpStatus status) {
    this.status = status;
  }

  /**
   * Returns the HTTP status this code is answered with.
   *
   * @return the bound HTTP status
   */
  public HttpStatus status() {
    return status;
  }

  /**
   * Picks a generic code for a status raised by the framework when no specific code applies.
   *
   * @param statusCode numeric HTTP status
   * @return the matching generic code, {@link #INTERNAL_SERVER_ERROR} for 5xx and {@link
   *     #BAD_REQUEST} for any other client error
   */
  public static ErrorCode fromStatus(int statusCode) {
    return switch (statusCode) {
      case 401 -> AUTHENTICATION_REQUIRED;
      case 403 -> ACCESS_DENIED;
      case 404 -> RESOURCE_NOT_FOUND;
      case 405 -> METHOD_NOT_ALLOWED;
      case 406 -> NOT_ACCEPTABLE;
      case 409 -> DATA_CONFLICT;
      case 415 -> UNSUPPORTED_MEDIA_TYPE;
      default -> statusCode >= 500 ? INTERNAL_SERVER_ERROR : BAD_REQUEST;
    };
  }
}

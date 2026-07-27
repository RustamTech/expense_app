package com.example.notes.application.coolproject.exception;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  // Our custom exceptions:
  @ExceptionHandler(BaseException.class)
  public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex) {
    return ResponseEntity.status(ex.getHttpStatus())
        .body(
            new ErrorResponse(
                ex.getMessage(),
                ex.getErrorCode().name(),
                LocalDateTime.now(),
                ex.getHttpStatus().value()));
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
    log.warn("Authentication failed: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(
            new ErrorResponse(
                "Invalid email or password",
                ErrorCode.AUTHENTICATION_FAILED.name(),
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception ex) {
    log.error("Internal error: ", ex);
    return ResponseEntity.status((HttpStatus.INTERNAL_SERVER_ERROR))
        .body(
            new com.example.notes.application.coolproject.exception.ErrorResponse(
                "Internal server error occurred",
                ErrorCode.INTERNAL_SERVER_ERROR.name(),
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            new ErrorResponse(
                "Malformed or unreadable JSON request body",
                ErrorCode.INVALID_REQUEST_DATA.name(),
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value()));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    String message = "Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName() + "'";
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            new ErrorResponse(
                message,
                ErrorCode.INVALID_REQUEST_DATA.name(),
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value()));
  }

  // Errors with validation
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));

    return ResponseEntity.status((HttpStatus.BAD_REQUEST))
        .body(
            new ErrorResponse(
                message,
                ErrorCode.INVALID_REQUEST_DATA.name(),
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value()));
  }
}

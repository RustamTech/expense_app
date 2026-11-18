package com.example.notes.application.coolproject.exception;

import com.example.notes.application.coolproject.security.InvalidTokenException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Translates every exception into an RFC 9457 {@link ProblemDetail} response.
 *
 * <p>Framework errors (unknown URL, wrong HTTP method, unsupported media type, missing parameters,
 * unreadable body, validation) are handled by {@link ResponseEntityExceptionHandler}; this class
 * refines their messages and adds two extra properties to every body:
 *
 * <ul>
 *   <li>{@code code} — a stable {@link ErrorCode} clients can branch on;
 *   <li>{@code timestamp} — when the error happened.
 * </ul>
 *
 * Security errors raised in the filter chain are routed here too (see {@code SecurityConfig}), so
 * 401 and 403 responses share the same format. Internal details are logged, never returned.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  static final String CODE = "code";
  static final String TIMESTAMP = "timestamp";
  static final String ERRORS = "errors";

  /** Echoed user input is truncated so error bodies stay small. */
  private static final int MAX_ECHOED_VALUE_LENGTH = 50;

  /**
   * Handles business exceptions thrown by services.
   *
   * @param ex the business exception
   * @return problem response with the exception's code and message
   */
  @ExceptionHandler(BaseException.class)
  public ResponseEntity<ProblemDetail> handleBusinessException(BaseException ex) {
    log.debug("Business error {}: {}", ex.getErrorCode(), ex.getMessage());
    return problem(ex.getErrorCode(), ex.getMessage());
  }

  /**
   * Handles failed authentication: bad login credentials, a missing token or an invalid token.
   *
   * @param ex the authentication exception
   * @return 401 problem response; login failures never reveal whether the email exists
   */
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ProblemDetail> handleAuthenticationException(AuthenticationException ex) {
    log.debug("Authentication failed: {}", ex.getClass().getSimpleName());
    return switch (ex) {
      case InvalidTokenException e ->
          problem(ErrorCode.INVALID_TOKEN, "The access token is invalid or expired");
      case InsufficientAuthenticationException e ->
          problem(
              ErrorCode.AUTHENTICATION_REQUIRED,
              "Authentication is required to access this resource");
      default -> problem(ErrorCode.INVALID_CREDENTIALS, "Invalid email or password");
    };
  }

  /**
   * Handles requests of an authenticated user that lacks the required role.
   *
   * @param ex the access-denied exception
   * @return 403 problem response
   */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex) {
    log.debug("Access denied: {}", ex.getMessage());
    return problem(ErrorCode.ACCESS_DENIED, "You do not have permission to access this resource");
  }

  /**
   * Handles database constraint violations that slipped past service checks (for example two
   * concurrent registrations with the same email).
   *
   * @param ex the integrity violation
   * @return 409 problem response without database details
   */
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ProblemDetail> handleDataIntegrityViolation(
      DataIntegrityViolationException ex) {
    log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
    return problem(ErrorCode.DATA_CONFLICT, "The request conflicts with existing data");
  }

  /**
   * Handles Bean Validation failures raised outside request-body binding.
   *
   * @param ex the constraint violation
   * @return 400 problem response listing every violation
   */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException ex) {
    List<FieldViolation> violations =
        ex.getConstraintViolations().stream()
            .map(v -> new FieldViolation(v.getPropertyPath().toString(), v.getMessage()))
            .toList();
    return validationProblem(violations);
  }

  /**
   * Last-resort handler for anything unexpected.
   *
   * @param ex the unexpected exception
   * @return 500 problem response with a generic message; the stack trace is only logged
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex) {
    log.error("Unexpected error", ex);
    return problem(ErrorCode.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
  }

  /** {@inheritDoc} Lists every invalid field in {@code errors}. */
  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    Stream<FieldViolation> fieldErrors =
        ex.getFieldErrors().stream()
            .map(e -> new FieldViolation(e.getField(), e.getDefaultMessage()));
    Stream<FieldViolation> globalErrors =
        ex.getGlobalErrors().stream()
            .map(e -> new FieldViolation(e.getObjectName(), e.getDefaultMessage()));
    return asObject(validationProblem(Stream.concat(fieldErrors, globalErrors).toList()));
  }

  /** {@inheritDoc} Lists every invalid method parameter in {@code errors}. */
  @Override
  protected ResponseEntity<Object> handleHandlerMethodValidationException(
      HandlerMethodValidationException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    List<FieldViolation> violations =
        ex.getParameterValidationResults().stream()
            .flatMap(
                result ->
                    result.getResolvableErrors().stream()
                        .map(
                            error ->
                                new FieldViolation(
                                    result.getMethodParameter().getParameterName(),
                                    error.getDefaultMessage())))
            .toList();
    return asObject(validationProblem(violations));
  }

  /** {@inheritDoc} Explains what exactly is wrong with the JSON body. */
  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    return asObject(problem(ErrorCode.MALFORMED_JSON, describeUnreadableBody(ex.getCause())));
  }

  /** {@inheritDoc} Names the parameter and, for enums, the allowed values. */
  @Override
  protected ResponseEntity<Object> handleTypeMismatch(
      TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
    String detail =
        "Invalid value '%s' for parameter '%s'"
                .formatted(truncate(ex.getValue()), ex.getPropertyName())
            + allowedValuesHint(ex.getRequiredType());
    return asObject(problem(ErrorCode.INVALID_PARAMETER, detail));
  }

  /** {@inheritDoc} Hides the "static resource" wording Spring uses for unmapped URLs. */
  @Override
  protected ResponseEntity<Object> handleNoResourceFoundException(
      NoResourceFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
    String detail = "No endpoint %s /%s".formatted(ex.getHttpMethod(), ex.getResourcePath());
    return asObject(problem(ErrorCode.RESOURCE_NOT_FOUND, detail));
  }

  /**
   * {@inheritDoc} Adds {@code code} and {@code timestamp} to every framework-generated problem
   * (405, 406, 415, missing parameters, ...).
   */
  @Override
  protected ResponseEntity<Object> handleExceptionInternal(
      Exception ex,
      @Nullable Object body,
      HttpHeaders headers,
      HttpStatusCode statusCode,
      WebRequest request) {
    ResponseEntity<Object> response =
        super.handleExceptionInternal(ex, body, headers, statusCode, request);
    if (response != null && response.getBody() instanceof ProblemDetail problemDetail) {
      enrich(problemDetail, frameworkErrorCode(ex, statusCode));
    }
    log.debug("Request rejected with {}: {}", statusCode.value(), ex.getMessage());
    return response;
  }

  private static ErrorCode frameworkErrorCode(Exception ex, HttpStatusCode statusCode) {
    return ex instanceof MissingRequestValueException
        ? ErrorCode.MISSING_PARAMETER
        : ErrorCode.fromStatus(statusCode.value());
  }

  private static String describeUnreadableBody(@Nullable Throwable cause) {
    return switch (cause) {
      case null -> "Request body is missing or unreadable";
      case InvalidFormatException e ->
          "Invalid value '%s' for field '%s'".formatted(truncate(e.getValue()), jsonPath(e))
              + allowedValuesHint(e.getTargetType());
      case MismatchedInputException e -> "Invalid type for field '%s'".formatted(jsonPath(e));
      case JsonProcessingException e -> "Malformed JSON request body";
      default -> "Request body is missing or unreadable";
    };
  }

  private static String jsonPath(JsonMappingException e) {
    return e.getPath().stream()
        .map(ref -> ref.getFieldName() != null ? ref.getFieldName() : "[" + ref.getIndex() + "]")
        .collect(Collectors.joining("."));
  }

  private static String allowedValuesHint(@Nullable Class<?> type) {
    if (type == null || !type.isEnum()) {
      return "";
    }
    return ". Allowed values: " + Arrays.toString(type.getEnumConstants());
  }

  private static String truncate(@Nullable Object value) {
    String text = String.valueOf(value);
    return text.length() <= MAX_ECHOED_VALUE_LENGTH
        ? text
        : text.substring(0, MAX_ECHOED_VALUE_LENGTH) + "...";
  }

  private static ResponseEntity<ProblemDetail> validationProblem(List<FieldViolation> violations) {
    ProblemDetail body =
        ProblemDetail.forStatusAndDetail(
            ErrorCode.VALIDATION_FAILED.status(), "Request validation failed");
    enrich(body, ErrorCode.VALIDATION_FAILED);
    body.setProperty(ERRORS, violations);
    return ResponseEntity.status(body.getStatus()).body(body);
  }

  private static ResponseEntity<ProblemDetail> problem(ErrorCode code, String detail) {
    ProblemDetail body = ProblemDetail.forStatusAndDetail(code.status(), detail);
    enrich(body, code);
    return ResponseEntity.status(code.status()).body(body);
  }

  private static void enrich(ProblemDetail body, ErrorCode code) {
    body.setProperty(CODE, code.name());
    body.setProperty(TIMESTAMP, Instant.now());
  }

  private static ResponseEntity<Object> asObject(ResponseEntity<ProblemDetail> response) {
    return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
  }
}

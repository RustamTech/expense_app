package com.example.notes.application.coolproject.exception;

/**
 * A single validation failure, listed in the {@code errors} property of a 400 response.
 *
 * @param field name of the invalid field or parameter
 * @param message human-readable reason
 */
public record FieldViolation(String field, String message) {}

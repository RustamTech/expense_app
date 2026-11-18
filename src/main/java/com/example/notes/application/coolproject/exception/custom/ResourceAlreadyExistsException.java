package com.example.notes.application.coolproject.exception.custom;

import com.example.notes.application.coolproject.exception.BaseException;
import com.example.notes.application.coolproject.exception.ErrorCode;

/** Thrown when a unique value (email, category name) is already taken. */
public class ResourceAlreadyExistsException extends BaseException {

  /**
   * Creates the exception.
   *
   * @param message client-safe description of the conflicting value
   */
  public ResourceAlreadyExistsException(String message) {
    super(ErrorCode.RESOURCE_ALREADY_EXISTS, message);
  }
}

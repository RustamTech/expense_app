package com.example.notes.application.coolproject.exception.custom;

import com.example.notes.application.coolproject.exception.BaseException;
import com.example.notes.application.coolproject.exception.ErrorCode;

/**
 * Thrown when a request is valid and authorized but breaks a business rule, for example an
 * administrator trying to demote, disable or delete their own account.
 */
public class OperationNotAllowedException extends BaseException {

  /**
   * Creates the exception.
   *
   * @param message client-safe description of the violated rule
   */
  public OperationNotAllowedException(String message) {
    super(ErrorCode.OPERATION_NOT_ALLOWED, message);
  }
}

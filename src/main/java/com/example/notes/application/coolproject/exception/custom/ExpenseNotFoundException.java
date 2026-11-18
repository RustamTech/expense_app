package com.example.notes.application.coolproject.exception.custom;

import com.example.notes.application.coolproject.exception.BaseException;
import com.example.notes.application.coolproject.exception.ErrorCode;

/**
 * Thrown when an expense does not exist or belongs to another user. Both cases look the same to the
 * client, so the existence of foreign expenses is not revealed.
 */
public class ExpenseNotFoundException extends BaseException {

  /**
   * Creates the exception for a missing or foreign expense.
   *
   * @param id identifier that was looked up
   */
  public ExpenseNotFoundException(Long id) {
    super(ErrorCode.EXPENSE_NOT_FOUND, "Expense with id " + id + " not found");
  }
}

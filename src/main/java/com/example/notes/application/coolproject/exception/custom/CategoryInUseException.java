package com.example.notes.application.coolproject.exception.custom;

import com.example.notes.application.coolproject.exception.BaseException;
import com.example.notes.application.coolproject.exception.ErrorCode;

/** Thrown when a category cannot be deleted because expenses still reference it. */
public class CategoryInUseException extends BaseException {

  /**
   * Creates the exception.
   *
   * @param id identifier of the referenced category
   */
  public CategoryInUseException(Long id) {
    super(
        ErrorCode.CATEGORY_IN_USE,
        "Category with id " + id + " is used by existing expenses and cannot be deleted");
  }
}

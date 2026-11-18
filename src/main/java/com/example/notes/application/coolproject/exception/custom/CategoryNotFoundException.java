package com.example.notes.application.coolproject.exception.custom;

import com.example.notes.application.coolproject.exception.BaseException;
import com.example.notes.application.coolproject.exception.ErrorCode;

/** Thrown when a category does not exist. */
public class CategoryNotFoundException extends BaseException {

  /**
   * Creates the exception for a missing category.
   *
   * @param id identifier that was looked up
   */
  public CategoryNotFoundException(Long id) {
    super(ErrorCode.CATEGORY_NOT_FOUND, "Category with id " + id + " not found");
  }
}

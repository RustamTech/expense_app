package com.example.notes.application.coolproject.exception.custom;

import com.example.notes.application.coolproject.exception.BaseException;
import com.example.notes.application.coolproject.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class CategoryNotFoundException extends BaseException {

  public CategoryNotFoundException(Long id) {
    super(
        ErrorCode.CATEGORY_NOT_FOUND,
        HttpStatus.NOT_FOUND,
        "Category with this id " + id + " not found");
  }
}

package com.example.notes.application.coolproject.exception.custom;

import com.example.notes.application.coolproject.exception.BaseException;
import com.example.notes.application.coolproject.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BaseException {

  public UserNotFoundException(String email) {
    super(ErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND, "User not found with email: " + email);
  }
}

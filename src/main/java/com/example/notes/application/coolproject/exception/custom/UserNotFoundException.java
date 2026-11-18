package com.example.notes.application.coolproject.exception.custom;

import com.example.notes.application.coolproject.exception.BaseException;
import com.example.notes.application.coolproject.exception.ErrorCode;

/** Thrown when a user account does not exist. */
public class UserNotFoundException extends BaseException {

  private UserNotFoundException(String message) {
    super(ErrorCode.USER_NOT_FOUND, message);
  }

  /**
   * Creates the exception for a user looked up by email (the authenticated principal).
   *
   * @param email email that was looked up
   * @return the exception
   */
  public static UserNotFoundException byEmail(String email) {
    return new UserNotFoundException("User not found with email: " + email);
  }

  /**
   * Creates the exception for a user looked up by identifier (admin operations).
   *
   * @param id identifier that was looked up
   * @return the exception
   */
  public static UserNotFoundException byId(Long id) {
    return new UserNotFoundException("User with id " + id + " not found");
  }
}

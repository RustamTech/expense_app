package com.example.notes.application.coolproject.service.user;

import com.example.notes.application.coolproject.dto.user.UserResponse;
import com.example.notes.application.coolproject.dto.user.UserUpdateRequest;
import com.example.notes.application.coolproject.exception.custom.UserNotFoundException;

/** Self-service operations on the authenticated user's own profile. */
public interface UserService {

  /**
   * Returns the profile of the authenticated user.
   *
   * @param email email of the authenticated user
   * @return the profile
   * @throws UserNotFoundException if the account no longer exists
   */
  UserResponse getCurrentUser(String email);

  /**
   * Replaces the editable profile fields of the authenticated user.
   *
   * @param email email of the authenticated user
   * @param request new first name, last name and phone
   * @return the updated profile
   * @throws UserNotFoundException if the account no longer exists
   */
  UserResponse updateCurrentUser(String email, UserUpdateRequest request);
}

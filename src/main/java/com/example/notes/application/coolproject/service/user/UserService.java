package com.example.notes.application.coolproject.service.user;

import com.example.notes.application.coolproject.dto.user.UserResponse;
import com.example.notes.application.coolproject.dto.user.UserUpdateRequest;

public interface UserService {
  UserResponse getCurrentUser(String email);

  UserResponse updateCurrentUser(String email, UserUpdateRequest request);
}

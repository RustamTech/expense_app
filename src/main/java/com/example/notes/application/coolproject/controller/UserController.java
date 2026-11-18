package com.example.notes.application.coolproject.controller;

import com.example.notes.application.coolproject.config.Endpoints;
import com.example.notes.application.coolproject.dto.user.UserResponse;
import com.example.notes.application.coolproject.dto.user.UserUpdateRequest;
import com.example.notes.application.coolproject.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users", description = "User profile management")
@RestController
@RequestMapping(Endpoints.USERS)
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @Operation(summary = "Get current user profile")
  @GetMapping(Endpoints.USER_ME)
  public ResponseEntity<UserResponse> getMe(Authentication authentication) {
    return ResponseEntity.ok(userService.getCurrentUser(authentication.getName()));
  }

  @Operation(summary = "Update current user profile")
  @PutMapping(Endpoints.USER_ME)
  public ResponseEntity<UserResponse> updateMe(
      Authentication authentication, @Valid @RequestBody UserUpdateRequest request) {
    return ResponseEntity.ok(userService.updateCurrentUser(authentication.getName(), request));
  }
}

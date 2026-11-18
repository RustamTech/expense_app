package com.example.notes.application.coolproject.controller;

import com.example.notes.application.coolproject.config.Endpoints;
import com.example.notes.application.coolproject.dto.user.UserResponse;
import com.example.notes.application.coolproject.dto.user.UserUpdateRequest;
import com.example.notes.application.coolproject.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

/** Own profile of the authenticated user. */
@Tag(name = "Users", description = "Own profile")
@ApiResponse(responseCode = "401", description = "Missing or invalid token")
@RestController
@RequestMapping(Endpoints.USERS)
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  /**
   * Returns the own profile.
   *
   * @param authentication the caller
   * @return the profile
   */
  @Operation(summary = "Get the current user profile")
  @ApiResponse(responseCode = "200", description = "Profile")
  @GetMapping(Endpoints.USER_ME)
  public ResponseEntity<UserResponse> getMe(Authentication authentication) {
    return ResponseEntity.ok(userService.getCurrentUser(authentication.getName()));
  }

  /**
   * Replaces the editable fields of the own profile.
   *
   * @param authentication the caller
   * @param request new first name, last name and phone
   * @return the updated profile
   */
  @Operation(summary = "Update the current user profile")
  @ApiResponse(responseCode = "200", description = "Profile updated")
  @ApiResponse(responseCode = "400", description = "Invalid request body")
  @PutMapping(Endpoints.USER_ME)
  public ResponseEntity<UserResponse> updateMe(
      Authentication authentication, @Valid @RequestBody UserUpdateRequest request) {
    return ResponseEntity.ok(userService.updateCurrentUser(authentication.getName(), request));
  }
}

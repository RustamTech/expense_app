package com.example.notes.application.coolproject.controller;

import com.example.notes.application.coolproject.config.Endpoints;
import com.example.notes.application.coolproject.dto.PageResponse;
import com.example.notes.application.coolproject.dto.admin.RoleUpdateRequest;
import com.example.notes.application.coolproject.dto.admin.StatusUpdateRequest;
import com.example.notes.application.coolproject.dto.user.UserResponse;
import com.example.notes.application.coolproject.service.admin.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** User management. Every endpoint requires the {@code ADMIN} role (see {@code SecurityConfig}). */
@Tag(name = "Admin: users", description = "User management, ADMIN role only")
@ApiResponse(responseCode = "401", description = "Missing or invalid token")
@ApiResponse(responseCode = "403", description = "Caller is not an administrator")
@RestController
@RequestMapping(Endpoints.ADMIN_USERS)
@RequiredArgsConstructor
public class AdminUserController {

  private static final int DEFAULT_PAGE_SIZE = 20;

  private final AdminUserService adminUserService;

  /**
   * Lists all accounts.
   *
   * @param pageable {@code page}, {@code size} and {@code sort} query parameters
   * @return one page of accounts
   */
  @Operation(summary = "List all users")
  @ApiResponse(responseCode = "200", description = "Page of users")
  @GetMapping
  public ResponseEntity<PageResponse<UserResponse>> findAll(
      @ParameterObject
          @PageableDefault(size = DEFAULT_PAGE_SIZE, sort = "id", direction = Sort.Direction.ASC)
          Pageable pageable) {
    return ResponseEntity.ok(adminUserService.findAll(pageable));
  }

  /**
   * Returns one account.
   *
   * @param id account identifier
   * @return the account
   */
  @Operation(summary = "Get a user by id")
  @ApiResponse(responseCode = "200", description = "User found")
  @ApiResponse(responseCode = "404", description = "User not found")
  @GetMapping(Endpoints.ADMIN_USER_BY_ID)
  public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
    return ResponseEntity.ok(adminUserService.findById(id));
  }

  /**
   * Changes the role of an account.
   *
   * @param authentication the calling administrator
   * @param id account identifier
   * @param request new role
   * @return the updated account
   */
  @Operation(summary = "Change the role of a user")
  @ApiResponse(responseCode = "200", description = "Role changed")
  @ApiResponse(responseCode = "404", description = "User not found")
  @ApiResponse(responseCode = "409", description = "Administrator targeted their own account")
  @PatchMapping(Endpoints.ADMIN_USER_ROLE)
  public ResponseEntity<UserResponse> changeRole(
      Authentication authentication,
      @PathVariable Long id,
      @Valid @RequestBody RoleUpdateRequest request) {
    return ResponseEntity.ok(
        adminUserService.changeRole(authentication.getName(), id, request.role()));
  }

  /**
   * Blocks or unblocks an account.
   *
   * @param authentication the calling administrator
   * @param id account identifier
   * @param request new status
   * @return the updated account
   */
  @Operation(summary = "Block or unblock a user")
  @ApiResponse(responseCode = "200", description = "Status changed")
  @ApiResponse(responseCode = "404", description = "User not found")
  @ApiResponse(responseCode = "409", description = "Administrator targeted their own account")
  @PatchMapping(Endpoints.ADMIN_USER_STATUS)
  public ResponseEntity<UserResponse> changeStatus(
      Authentication authentication,
      @PathVariable Long id,
      @Valid @RequestBody StatusUpdateRequest request) {
    return ResponseEntity.ok(
        adminUserService.changeStatus(authentication.getName(), id, request.enabled()));
  }

  /**
   * Deletes an account and all its expenses.
   *
   * @param authentication the calling administrator
   * @param id account identifier
   * @return empty 204 response
   */
  @Operation(summary = "Delete a user and all their expenses")
  @ApiResponse(responseCode = "204", description = "User deleted")
  @ApiResponse(responseCode = "404", description = "User not found")
  @ApiResponse(responseCode = "409", description = "Administrator targeted their own account")
  @DeleteMapping(Endpoints.ADMIN_USER_BY_ID)
  public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
    adminUserService.delete(authentication.getName(), id);
    return ResponseEntity.noContent().build();
  }
}

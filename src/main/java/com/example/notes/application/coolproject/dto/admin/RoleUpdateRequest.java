package com.example.notes.application.coolproject.dto.admin;

import com.example.notes.application.coolproject.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Changes the role of a user (administrators only).
 *
 * @param role new role
 */
public record RoleUpdateRequest(@Schema(example = "ADMIN") @NotNull UserRole role) {}

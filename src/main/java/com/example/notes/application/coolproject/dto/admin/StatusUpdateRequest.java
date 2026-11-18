package com.example.notes.application.coolproject.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Enables or blocks a user (administrators only). A blocked user cannot log in and existing tokens
 * stop working immediately.
 *
 * @param enabled {@code false} to block, {@code true} to unblock
 */
public record StatusUpdateRequest(@Schema(example = "false") @NotNull Boolean enabled) {}

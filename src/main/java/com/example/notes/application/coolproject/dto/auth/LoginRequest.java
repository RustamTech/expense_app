package com.example.notes.application.coolproject.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Login payload.
 *
 * @param email login email, case-insensitive
 * @param password raw password
 */
public record LoginRequest(
    @Schema(example = "user@example.com") @NotBlank @Email @Size(max = 254) String email,
    @Schema(example = "Str0ngPassw0rd") @NotBlank @Size(max = 72) String password) {}

package com.example.notes.application.coolproject.dto.auth;

import com.example.notes.application.coolproject.dto.PhoneNumber;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Registration payload. New accounts always get the {@code USER} role.
 *
 * @param firstName first name
 * @param lastName last name
 * @param email login email; stored trimmed and lower-cased
 * @param phoneNumber phone in international format
 * @param password raw password, 8–72 characters (BCrypt ignores anything beyond 72 bytes)
 */
public record RegisterRequest(
    @Schema(example = "Rustam") @NotBlank @Size(max = 50) String firstName,
    @Schema(example = "Ismayilov") @NotBlank @Size(max = 50) String lastName,
    @Schema(example = "user@example.com") @NotBlank @Email @Size(max = 254) String email,
    @Schema(example = "+994501234567")
        @NotBlank @Pattern(regexp = PhoneNumber.PATTERN, message = PhoneNumber.MESSAGE) String phoneNumber,
    @Schema(example = "Str0ngPassw0rd") @NotBlank @Size(min = 8, max = 72) String password) {}

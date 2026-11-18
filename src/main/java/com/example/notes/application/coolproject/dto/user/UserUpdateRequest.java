package com.example.notes.application.coolproject.dto.user;

import com.example.notes.application.coolproject.dto.PhoneNumber;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Full update of the own profile. Email, password and role cannot be changed here.
 *
 * @param firstName first name
 * @param lastName last name
 * @param phoneNumber phone in international format
 */
public record UserUpdateRequest(
    @Schema(example = "Rustam") @NotBlank @Size(max = 50) String firstName,
    @Schema(example = "Ismayilov") @NotBlank @Size(max = 50) String lastName,
    @Schema(example = "+994501234567")
        @NotBlank @Pattern(regexp = PhoneNumber.PATTERN, message = PhoneNumber.MESSAGE) String phoneNumber) {}

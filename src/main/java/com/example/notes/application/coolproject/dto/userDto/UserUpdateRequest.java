package com.example.notes.application.coolproject.dto.userDto;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequest(
    @NotBlank String firstName, @NotBlank String lastName, @NotBlank String phoneNumber) {}

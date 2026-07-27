package com.example.notes.application.coolproject.dto.userDto;

import com.example.notes.application.coolproject.enums.UserRole;
import java.time.LocalDateTime;

public record UserResponse(
    Long id,
    String firstName,
    String lastName,
    String email,
    String phoneNumber,
    UserRole userRole,
    LocalDateTime createdAt) {}

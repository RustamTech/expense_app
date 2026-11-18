package com.example.notes.application.coolproject.dto.user;

import com.example.notes.application.coolproject.enums.UserRole;
import java.time.LocalDateTime;

/**
 * Public view of an account. Never contains the password hash.
 *
 * @param id identifier
 * @param firstName first name
 * @param lastName last name
 * @param email email
 * @param phoneNumber phone number
 * @param userRole role
 * @param enabled whether the account may log in
 * @param createdAt registration time
 */
public record UserResponse(
    Long id,
    String firstName,
    String lastName,
    String email,
    String phoneNumber,
    UserRole userRole,
    boolean enabled,
    LocalDateTime createdAt) {}

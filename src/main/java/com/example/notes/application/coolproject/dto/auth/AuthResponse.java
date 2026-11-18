package com.example.notes.application.coolproject.dto.auth;

import com.example.notes.application.coolproject.enums.UserRole;

/**
 * Result of a successful registration or login.
 *
 * @param token JWT access token, sent back as {@code Authorization: Bearer <token>}
 * @param email email of the account
 * @param firstName first name
 * @param lastName last name
 * @param role role of the account, so clients can show or hide admin features
 */
public record AuthResponse(
    String token, String email, String firstName, String lastName, UserRole role) {}

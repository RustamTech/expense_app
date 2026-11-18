package com.example.notes.application.coolproject.controller;

import com.example.notes.application.coolproject.config.Endpoints;
import com.example.notes.application.coolproject.dto.auth.AuthResponse;
import com.example.notes.application.coolproject.dto.auth.LoginRequest;
import com.example.notes.application.coolproject.dto.auth.RegisterRequest;
import com.example.notes.application.coolproject.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public registration and login endpoints. */
@Tag(name = "Authentication", description = "Registration and login")
@SecurityRequirements
@ApiResponse(responseCode = "400", description = "Invalid or malformed request body")
@RestController
@RequestMapping(Endpoints.AUTH)
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  /**
   * Registers a new {@code USER} account.
   *
   * @param request registration payload
   * @return 201 with a token for the new account
   */
  @Operation(summary = "Register a new user")
  @ApiResponse(responseCode = "201", description = "User registered, token issued")
  @ApiResponse(responseCode = "409", description = "Email already registered")
  @PostMapping(Endpoints.AUTH_REGISTER)
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
  }

  /**
   * Logs in with email and password.
   *
   * @param request login payload
   * @return 200 with a token
   */
  @Operation(summary = "Log in")
  @ApiResponse(responseCode = "200", description = "Token issued")
  @ApiResponse(responseCode = "401", description = "Invalid email or password")
  @PostMapping(Endpoints.AUTH_LOGIN)
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }
}

package com.example.notes.application.coolproject.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.notes.application.coolproject.config.SecurityConfig;
import com.example.notes.application.coolproject.dto.auth.AuthResponse;
import com.example.notes.application.coolproject.dto.auth.LoginRequest;
import com.example.notes.application.coolproject.dto.auth.RegisterRequest;
import com.example.notes.application.coolproject.service.security.AuthService;
import com.example.notes.application.coolproject.service.security.JwtService;
import com.example.notes.application.coolproject.service.security.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private AuthService authService;

  @MockBean private JwtService jwtService;

  @MockBean private UserDetailsServiceImpl userDetailsService;

  @Test
  void register_validRequest_returns201() throws Exception {
    RegisterRequest request =
        new RegisterRequest("John", "Doe", "john@test.com", "+1234567890", "password123");
    AuthResponse response = new AuthResponse("jwt-token", "john@test.com", "John", "Doe");

    when(authService.register(any())).thenReturn(response);

    mockMvc
        .perform(
            post("/api/notes/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.token").value("jwt-token"))
        .andExpect(jsonPath("$.email").value("john@test.com"));
  }

  @Test
  void register_blankFirstName_returns400() throws Exception {
    RegisterRequest request =
        new RegisterRequest("", "Doe", "john@test.com", "+1234567890", "password123");

    mockMvc
        .perform(
            post("/api/notes/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void register_invalidEmail_returns400() throws Exception {
    RegisterRequest request =
        new RegisterRequest("John", "Doe", "not-an-email", "+1234567890", "password123");

    mockMvc
        .perform(
            post("/api/notes/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void register_shortPassword_returns400() throws Exception {
    RegisterRequest request =
        new RegisterRequest("John", "Doe", "john@test.com", "+1234567890", "short");

    mockMvc
        .perform(
            post("/api/notes/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void login_validRequest_returns200() throws Exception {
    LoginRequest request = new LoginRequest("john@test.com", "password123");
    AuthResponse response = new AuthResponse("jwt-token", "john@test.com", "John", "Doe");

    when(authService.login(any())).thenReturn(response);

    mockMvc
        .perform(
            post("/api/notes/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("jwt-token"));
  }
}

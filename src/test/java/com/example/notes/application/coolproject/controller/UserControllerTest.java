package com.example.notes.application.coolproject.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.notes.application.coolproject.config.SecurityConfig;
import com.example.notes.application.coolproject.dto.userDto.UserResponse;
import com.example.notes.application.coolproject.dto.userDto.UserUpdateRequest;
import com.example.notes.application.coolproject.enums.UserRole;
import com.example.notes.application.coolproject.service.security.JwtService;
import com.example.notes.application.coolproject.service.security.UserDetailsServiceImpl;
import com.example.notes.application.coolproject.service.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private UserService userService;

  @MockBean private JwtService jwtService;

  @MockBean private UserDetailsServiceImpl userDetailsService;

  @Test
  @WithMockUser(username = "john@test.com")
  void getMe_authenticated_returns200() throws Exception {
    UserResponse response = buildUserResponse();
    when(userService.getCurrentUser("john@test.com")).thenReturn(response);

    mockMvc
        .perform(get("/api/notes/users/me"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("john@test.com"))
        .andExpect(jsonPath("$.firstName").value("John"));
  }

  @Test
  @WithMockUser(username = "john@test.com")
  void updateMe_validRequest_returns200() throws Exception {
    UserUpdateRequest request = new UserUpdateRequest("Jane", "Smith", "+9999999");
    UserResponse response =
        new UserResponse(
            1L, "Jane", "Smith", "john@test.com", "+9999999", UserRole.USER, LocalDateTime.now());

    when(userService.updateCurrentUser(eq("john@test.com"), any())).thenReturn(response);

    mockMvc
        .perform(
            put("/api/notes/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("Jane"));
  }

  @Test
  @WithMockUser(username = "john@test.com")
  void updateMe_blankFirstName_returns400() throws Exception {
    UserUpdateRequest request = new UserUpdateRequest("", "Smith", "+9999999");

    mockMvc
        .perform(
            put("/api/notes/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getMe_unauthenticated_returns401() throws Exception {
    mockMvc.perform(get("/api/notes/users/me")).andExpect(status().isUnauthorized());
  }

  private UserResponse buildUserResponse() {
    return new UserResponse(
        1L, "John", "Doe", "john@test.com", "+1234567890", UserRole.USER, LocalDateTime.now());
  }
}

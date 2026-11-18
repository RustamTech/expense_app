package com.example.notes.application.coolproject.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.notes.application.coolproject.dto.auth.AuthResponse;
import com.example.notes.application.coolproject.dto.auth.LoginRequest;
import com.example.notes.application.coolproject.dto.auth.RegisterRequest;
import com.example.notes.application.coolproject.enums.UserRole;
import com.example.notes.application.coolproject.exception.custom.ResourceAlreadyExistsException;
import com.example.notes.application.coolproject.service.auth.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(AuthController.class)
class AuthControllerTest extends AbstractControllerTest {

  private static final String REGISTER = "/api/notes/auth/register";
  private static final String LOGIN = "/api/notes/auth/login";
  private static final AuthResponse TOKEN =
      new AuthResponse("jwt", "user@example.com", "John", "Doe", UserRole.USER);

  @MockitoBean private AuthService authService;

  @Test
  void registerWithoutTokenReturns201AndToken() throws Exception {
    when(authService.register(any())).thenReturn(TOKEN);

    mockMvc
        .perform(post(REGISTER).contentType(JSON).content(json(validRegistration())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.token").value("jwt"))
        .andExpect(jsonPath("$.role").value("USER"));
  }

  @Test
  void registerWithInvalidFieldsReturns400AndSkipsService() throws Exception {
    var request = new RegisterRequest("", "Doe", "not-an-email", "abc", "short");

    mockMvc
        .perform(post(REGISTER).contentType(JSON).content(json(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
        .andExpect(jsonPath("$.errors.length()").value(4));
    verifyNoInteractions(authService);
  }

  @Test
  void registerWithTakenEmailReturns409() throws Exception {
    when(authService.register(any()))
        .thenThrow(new ResourceAlreadyExistsException("Email is already registered"));

    mockMvc
        .perform(post(REGISTER).contentType(JSON).content(json(validRegistration())))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("RESOURCE_ALREADY_EXISTS"));
  }

  @Test
  void loginWithValidCredentialsReturns200() throws Exception {
    when(authService.login(any())).thenReturn(TOKEN);

    mockMvc
        .perform(post(LOGIN).contentType(JSON).content(json(validLogin())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("jwt"));
  }

  @Test
  void loginWithWrongPasswordReturns401InvalidCredentials() throws Exception {
    when(authService.login(any())).thenThrow(new BadCredentialsException("Bad credentials"));

    mockMvc
        .perform(post(LOGIN).contentType(JSON).content(json(validLogin())))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
        .andExpect(jsonPath("$.detail").value("Invalid email or password"));
  }

  @Test
  void loginOfBlockedAccountLooksLikeWrongCredentials() throws Exception {
    when(authService.login(any())).thenThrow(new DisabledException("User is disabled"));

    mockMvc
        .perform(post(LOGIN).contentType(JSON).content(json(validLogin())))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
  }

  private static RegisterRequest validRegistration() {
    return new RegisterRequest("John", "Doe", "user@example.com", "+994501234567", "password1");
  }

  private static LoginRequest validLogin() {
    return new LoginRequest("user@example.com", "password1");
  }
}

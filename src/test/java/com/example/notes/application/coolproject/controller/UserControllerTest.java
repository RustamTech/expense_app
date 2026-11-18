package com.example.notes.application.coolproject.controller;

import static com.example.notes.application.coolproject.TestData.USER_EMAIL;
import static com.example.notes.application.coolproject.TestData.USER_ID;
import static com.example.notes.application.coolproject.TestData.userResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.notes.application.coolproject.dto.user.UserUpdateRequest;
import com.example.notes.application.coolproject.enums.UserRole;
import com.example.notes.application.coolproject.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(UserController.class)
class UserControllerTest extends AbstractControllerTest {

  private static final String ME = "/api/notes/users/me";

  @MockitoBean private UserService userService;

  @Test
  @WithMockUser(username = USER_EMAIL)
  void returnsOwnProfileWithoutPassword() throws Exception {
    when(userService.getCurrentUser(USER_EMAIL))
        .thenReturn(userResponse(USER_ID, USER_EMAIL, UserRole.USER));

    mockMvc
        .perform(get(ME))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(USER_EMAIL))
        .andExpect(jsonPath("$.userRole").value("USER"))
        .andExpect(jsonPath("$.password").doesNotExist());
  }

  @Test
  @WithMockUser(username = USER_EMAIL)
  void updatesOwnProfile() throws Exception {
    var request = new UserUpdateRequest("Jane", "Doe", "+994501234567");
    when(userService.updateCurrentUser(eq(USER_EMAIL), any()))
        .thenReturn(userResponse(USER_ID, USER_EMAIL, UserRole.USER));

    mockMvc.perform(put(ME).contentType(JSON).content(json(request))).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = USER_EMAIL)
  void invalidPhoneReturns400() throws Exception {
    var request = new UserUpdateRequest("Jane", "Doe", "call me");

    mockMvc
        .perform(put(ME).contentType(JSON).content(json(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].field").value("phoneNumber"));
    verifyNoInteractions(userService);
  }

  @Test
  void anonymousReturns401() throws Exception {
    mockMvc.perform(get(ME)).andExpect(status().isUnauthorized());
  }
}

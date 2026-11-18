package com.example.notes.application.coolproject.controller;

import static com.example.notes.application.coolproject.TestData.ADMIN_EMAIL;
import static com.example.notes.application.coolproject.TestData.ADMIN_ID;
import static com.example.notes.application.coolproject.TestData.USER_EMAIL;
import static com.example.notes.application.coolproject.TestData.USER_ID;
import static com.example.notes.application.coolproject.TestData.userResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.notes.application.coolproject.dto.PageResponse;
import com.example.notes.application.coolproject.enums.UserRole;
import com.example.notes.application.coolproject.exception.custom.OperationNotAllowedException;
import com.example.notes.application.coolproject.exception.custom.UserNotFoundException;
import com.example.notes.application.coolproject.service.admin.AdminUserService;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(AdminUserController.class)
class AdminUserControllerTest extends AbstractControllerTest {

  private static final String USERS = "/api/notes/admin/users";

  @MockitoBean private AdminUserService adminUserService;

  @Nested
  @WithMockUser(username = ADMIN_EMAIL, roles = "ADMIN")
  class AsAdmin {

    @Test
    void listsUsersPaged() throws Exception {
      var page =
          new PageResponse<>(
              List.of(userResponse(USER_ID, USER_EMAIL, UserRole.USER)), 0, 20, 1, 1);
      when(adminUserService.findAll(any(Pageable.class))).thenReturn(page);

      mockMvc
          .perform(get(USERS).param("page", "0").param("size", "20"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content[0].email").value(USER_EMAIL))
          .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void missingUserReturns404() throws Exception {
      when(adminUserService.findById(9L)).thenThrow(UserNotFoundException.byId(9L));

      mockMvc
          .perform(get(USERS + "/9"))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }

    @Test
    void promotesUserToAdmin() throws Exception {
      when(adminUserService.changeRole(ADMIN_EMAIL, USER_ID, UserRole.ADMIN))
          .thenReturn(userResponse(USER_ID, USER_EMAIL, UserRole.ADMIN));

      mockMvc
          .perform(patch(USERS + "/1/role").contentType(JSON).content("{\"role\":\"ADMIN\"}"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.userRole").value("ADMIN"));
    }

    @Test
    void unknownRoleReturns400() throws Exception {
      mockMvc
          .perform(patch(USERS + "/1/role").contentType(JSON).content("{\"role\":\"ROOT\"}"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("MALFORMED_JSON"));
      verifyNoInteractions(adminUserService);
    }

    @Test
    void missingStatusReturns400() throws Exception {
      mockMvc
          .perform(patch(USERS + "/1/status").contentType(JSON).content("{}"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.errors[0].field").value("enabled"));
    }

    @Test
    void blocksUser() throws Exception {
      when(adminUserService.changeStatus(ADMIN_EMAIL, USER_ID, false))
          .thenReturn(userResponse(USER_ID, USER_EMAIL, UserRole.USER));

      mockMvc
          .perform(patch(USERS + "/1/status").contentType(JSON).content("{\"enabled\":false}"))
          .andExpect(status().isOk());
      verify(adminUserService).changeStatus(ADMIN_EMAIL, USER_ID, false);
    }

    @Test
    void changingOwnAccountReturns409() throws Exception {
      when(adminUserService.changeStatus(ADMIN_EMAIL, ADMIN_ID, false))
          .thenThrow(new OperationNotAllowedException("You cannot block your own account"));

      mockMvc
          .perform(patch(USERS + "/100/status").contentType(JSON).content("{\"enabled\":false}"))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.code").value("OPERATION_NOT_ALLOWED"));
    }

    @Test
    void deletesUser() throws Exception {
      mockMvc.perform(delete(USERS + "/1")).andExpect(status().isNoContent());

      verify(adminUserService).delete(ADMIN_EMAIL, USER_ID);
    }

    @Test
    void deletingOwnAccountReturns409() throws Exception {
      doThrow(new OperationNotAllowedException("You cannot delete your own account"))
          .when(adminUserService)
          .delete(ADMIN_EMAIL, ADMIN_ID);

      mockMvc.perform(delete(USERS + "/100")).andExpect(status().isConflict());
    }
  }

  @Test
  @WithMockUser(username = USER_EMAIL)
  void regularUserGets403() throws Exception {
    mockMvc
        .perform(get(USERS))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    verifyNoInteractions(adminUserService);
  }

  @Test
  @WithMockUser(username = USER_EMAIL)
  void regularUserCannotPromoteThemselves() throws Exception {
    mockMvc
        .perform(patch(USERS + "/1/role").contentType(JSON).content("{\"role\":\"ADMIN\"}"))
        .andExpect(status().isForbidden());
    verifyNoInteractions(adminUserService);
  }

  @Test
  void anonymousGets401() throws Exception {
    mockMvc.perform(get(USERS)).andExpect(status().isUnauthorized());
  }
}

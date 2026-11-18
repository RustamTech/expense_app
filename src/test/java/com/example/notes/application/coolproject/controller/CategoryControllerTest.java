package com.example.notes.application.coolproject.controller;

import static com.example.notes.application.coolproject.TestData.categoryResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.notes.application.coolproject.dto.category.CategoryCreateRequest;
import com.example.notes.application.coolproject.dto.category.CategoryUpdateRequest;
import com.example.notes.application.coolproject.enums.CategoryType;
import com.example.notes.application.coolproject.exception.custom.CategoryInUseException;
import com.example.notes.application.coolproject.exception.custom.CategoryNotFoundException;
import com.example.notes.application.coolproject.service.category.CategoryService;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest extends AbstractControllerTest {

  private static final String CATEGORIES = "/api/notes/categories";
  private static final CategoryCreateRequest CREATE =
      new CategoryCreateRequest("🍔", "#FF0000", "Food", CategoryType.EXPENSE);
  private static final CategoryUpdateRequest UPDATE =
      new CategoryUpdateRequest(null, null, "Pizza", null);

  @MockitoBean private CategoryService categoryService;

  @Nested
  @WithMockUser
  class AsUser {

    @Test
    void readsCatalog() throws Exception {
      when(categoryService.findAll())
          .thenReturn(List.of(categoryResponse(1L, "Food"), categoryResponse(2L, "Taxi")));

      mockMvc
          .perform(get(CATEGORIES))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(2))
          .andExpect(jsonPath("$[0].categoryType").value("EXPENSE"));
    }

    @Test
    void readsOneCategory() throws Exception {
      when(categoryService.findById(1L)).thenReturn(categoryResponse(1L, "Food"));

      mockMvc
          .perform(get(CATEGORIES + "/1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.name").value("Food"));
    }

    @Test
    void missingCategoryReturns404() throws Exception {
      when(categoryService.findById(99L)).thenThrow(new CategoryNotFoundException(99L));

      mockMvc
          .perform(get(CATEGORIES + "/99"))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"));
    }
  }

  @Nested
  @WithMockUser(roles = "ADMIN")
  class AsAdmin {

    @Test
    void createsCategory() throws Exception {
      when(categoryService.create(any())).thenReturn(categoryResponse(1L, "Food"));

      mockMvc
          .perform(post(CATEGORIES).contentType(JSON).content(json(CREATE)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void blankNameReturns400() throws Exception {
      var request = new CategoryCreateRequest("🍔", "#FF0000", " ", CategoryType.EXPENSE);

      mockMvc
          .perform(post(CATEGORIES).contentType(JSON).content(json(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.errors[0].field").value("name"));
    }

    @Test
    void blankNameInUpdateReturns400() throws Exception {
      var request = new CategoryUpdateRequest(null, null, "  ", null);

      mockMvc
          .perform(put(CATEGORIES + "/1").contentType(JSON).content(json(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.errors[0].message").value("must not be blank"));
    }

    @Test
    void updatesCategory() throws Exception {
      when(categoryService.update(eq(1L), any())).thenReturn(categoryResponse(1L, "Pizza"));

      mockMvc
          .perform(put(CATEGORIES + "/1").contentType(JSON).content(json(UPDATE)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.name").value("Pizza"));
    }

    @Test
    void deletesCategory() throws Exception {
      mockMvc.perform(delete(CATEGORIES + "/1")).andExpect(status().isNoContent());

      verify(categoryService).delete(1L);
    }

    @Test
    void deletingCategoryInUseReturns409() throws Exception {
      doThrow(new CategoryInUseException(1L)).when(categoryService).delete(1L);

      mockMvc
          .perform(delete(CATEGORIES + "/1"))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.code").value("CATEGORY_IN_USE"));
    }
  }

  @Test
  void anonymousCannotReadCatalog() throws Exception {
    mockMvc.perform(get(CATEGORIES)).andExpect(status().isUnauthorized());
  }
}

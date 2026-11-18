package com.example.notes.application.coolproject.controller;

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

import com.example.notes.application.coolproject.config.SecurityConfig;
import com.example.notes.application.coolproject.dto.category.CategoryCreateRequest;
import com.example.notes.application.coolproject.dto.category.CategoryResponse;
import com.example.notes.application.coolproject.dto.category.CategoryUpdateRequest;
import com.example.notes.application.coolproject.enums.CategoryType;
import com.example.notes.application.coolproject.exception.custom.CategoryNotFoundException;
import com.example.notes.application.coolproject.security.JwtService;
import com.example.notes.application.coolproject.security.UserDetailsServiceImpl;
import com.example.notes.application.coolproject.service.category.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CategoryController.class)
@Import(SecurityConfig.class)
class CategoryControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private CategoryService categoryService;

  @MockBean private JwtService jwtService;

  @MockBean private UserDetailsServiceImpl userDetailsService;

  @Test
  @WithMockUser
  void findAll_returns200WithList() throws Exception {
    List<CategoryResponse> categories =
        List.of(
            new CategoryResponse(1L, "red", "🍔", "Food"),
            new CategoryResponse(2L, "blue", "🚗", "Transport"));
    when(categoryService.findAll()).thenReturn(categories);

    mockMvc
        .perform(get("/api/notes/categories"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].name").value("Food"));
  }

  @Test
  @WithMockUser
  void findById_existing_returns200() throws Exception {
    CategoryResponse response = new CategoryResponse(1L, "red", "🍔", "Food");
    when(categoryService.findById(1L)).thenReturn(response);

    mockMvc
        .perform(get("/api/notes/categories/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("Food"));
  }

  @Test
  @WithMockUser
  void findById_notFound_returns404() throws Exception {
    when(categoryService.findById(99L)).thenThrow(new CategoryNotFoundException(99L));

    mockMvc.perform(get("/api/notes/categories/99")).andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser
  void create_validRequest_returns201() throws Exception {
    CategoryCreateRequest request =
        new CategoryCreateRequest("🍔", "red", "Food", CategoryType.EXPENSE);
    CategoryResponse response = new CategoryResponse(1L, "red", "🍔", "Food");

    when(categoryService.create(any())).thenReturn(response);

    mockMvc
        .perform(
            post("/api/notes/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("Food"));
  }

  @Test
  @WithMockUser
  void create_blankName_returns400() throws Exception {
    CategoryCreateRequest request =
        new CategoryCreateRequest("🍔", "red", "", CategoryType.EXPENSE);

    mockMvc
        .perform(
            post("/api/notes/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void update_existingId_returns200() throws Exception {
    CategoryUpdateRequest request =
        new CategoryUpdateRequest("🍕", "blue", "Pizza", CategoryType.EXPENSE);
    CategoryResponse response = new CategoryResponse(1L, "blue", "🍕", "Pizza");

    when(categoryService.update(eq(1L), any())).thenReturn(response);

    mockMvc
        .perform(
            put("/api/notes/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Pizza"));
  }

  @Test
  @WithMockUser
  void delete_existingId_returns204() throws Exception {
    mockMvc.perform(delete("/api/notes/categories/1")).andExpect(status().isNoContent());

    verify(categoryService).delete(1L);
  }

  @Test
  @WithMockUser
  void delete_notFound_returns404() throws Exception {
    doThrow(new CategoryNotFoundException(99L)).when(categoryService).delete(99L);

    mockMvc.perform(delete("/api/notes/categories/99")).andExpect(status().isNotFound());
  }

  @Test
  void findAll_unauthenticated_returns401() throws Exception {
    mockMvc.perform(get("/api/notes/categories")).andExpect(status().isUnauthorized());
  }
}

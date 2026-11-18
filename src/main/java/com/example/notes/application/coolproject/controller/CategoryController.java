package com.example.notes.application.coolproject.controller;

import com.example.notes.application.coolproject.config.Endpoints;
import com.example.notes.application.coolproject.dto.category.CategoryCreateRequest;
import com.example.notes.application.coolproject.dto.category.CategoryResponse;
import com.example.notes.application.coolproject.dto.category.CategoryUpdateRequest;
import com.example.notes.application.coolproject.service.category.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Categories", description = "Category management endpoints")
@RestController
@RequestMapping(Endpoints.CATEGORIES)
@RequiredArgsConstructor
public class CategoryController {

  private final CategoryService categoryService;

  @Operation(summary = "Get all categories")
  @GetMapping
  public ResponseEntity<List<CategoryResponse>> findAll() {
    return ResponseEntity.ok(categoryService.findAll());
  }

  @Operation(summary = "Get category by id")
  @GetMapping(Endpoints.CATEGORY_BY_ID)
  public ResponseEntity<CategoryResponse> findById(@PathVariable Long id) {
    return ResponseEntity.ok(categoryService.findById(id));
  }

  @Operation(summary = "Create new category")
  @PostMapping
  public ResponseEntity<CategoryResponse> create(
      @Valid @RequestBody CategoryCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(request));
  }

  @Operation(summary = "Update category")
  @PutMapping(Endpoints.CATEGORY_BY_ID)
  public ResponseEntity<CategoryResponse> update(
      @PathVariable Long id, @Valid @RequestBody CategoryUpdateRequest request) {
    return ResponseEntity.ok(categoryService.update(id, request));
  }

  @Operation(summary = "Delete category")
  @DeleteMapping(Endpoints.CATEGORY_BY_ID)
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    categoryService.delete(id);
    return ResponseEntity.noContent().build();
  }
}

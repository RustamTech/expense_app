package com.example.notes.application.coolproject.controller;

import com.example.notes.application.coolproject.config.Endpoints;
import com.example.notes.application.coolproject.dto.category.CategoryCreateRequest;
import com.example.notes.application.coolproject.dto.category.CategoryResponse;
import com.example.notes.application.coolproject.dto.category.CategoryUpdateRequest;
import com.example.notes.application.coolproject.service.category.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Shared category catalog: read by every user, managed by administrators. */
@Tag(name = "Categories", description = "Category catalog; changes require the ADMIN role")
@ApiResponse(responseCode = "401", description = "Missing or invalid token")
@RestController
@RequestMapping(Endpoints.CATEGORIES)
@RequiredArgsConstructor
public class CategoryController {

  private final CategoryService categoryService;

  /**
   * Returns the whole catalog.
   *
   * @return all categories sorted by name
   */
  @Operation(summary = "Get all categories")
  @ApiResponse(responseCode = "200", description = "Categories")
  @GetMapping
  public ResponseEntity<List<CategoryResponse>> findAll() {
    return ResponseEntity.ok(categoryService.findAll());
  }

  /**
   * Returns one category.
   *
   * @param id category identifier
   * @return the category
   */
  @Operation(summary = "Get a category by id")
  @ApiResponse(responseCode = "200", description = "Category found")
  @ApiResponse(responseCode = "404", description = "Category not found")
  @GetMapping(Endpoints.CATEGORY_BY_ID)
  public ResponseEntity<CategoryResponse> findById(@PathVariable Long id) {
    return ResponseEntity.ok(categoryService.findById(id));
  }

  /**
   * Adds a category (ADMIN).
   *
   * @param request create payload
   * @return 201 with the created category
   */
  @Operation(summary = "Create a category (ADMIN)")
  @ApiResponse(responseCode = "201", description = "Category created")
  @ApiResponse(responseCode = "400", description = "Invalid request body")
  @ApiResponse(responseCode = "403", description = "Caller is not an administrator")
  @ApiResponse(responseCode = "409", description = "Name already taken")
  @PostMapping
  public ResponseEntity<CategoryResponse> create(
      @Valid @RequestBody CategoryCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(request));
  }

  /**
   * Changes a category (ADMIN).
   *
   * @param id category identifier
   * @param request partial update payload
   * @return the updated category
   */
  @Operation(summary = "Update a category (ADMIN)")
  @ApiResponse(responseCode = "200", description = "Category updated")
  @ApiResponse(responseCode = "400", description = "Invalid request body")
  @ApiResponse(responseCode = "403", description = "Caller is not an administrator")
  @ApiResponse(responseCode = "404", description = "Category not found")
  @ApiResponse(responseCode = "409", description = "Name already taken")
  @PutMapping(Endpoints.CATEGORY_BY_ID)
  public ResponseEntity<CategoryResponse> update(
      @PathVariable Long id, @Valid @RequestBody CategoryUpdateRequest request) {
    return ResponseEntity.ok(categoryService.update(id, request));
  }

  /**
   * Removes an unused category (ADMIN).
   *
   * @param id category identifier
   * @return empty 204 response
   */
  @Operation(summary = "Delete a category (ADMIN)")
  @ApiResponse(responseCode = "204", description = "Category deleted")
  @ApiResponse(responseCode = "403", description = "Caller is not an administrator")
  @ApiResponse(responseCode = "404", description = "Category not found")
  @ApiResponse(responseCode = "409", description = "Category is used by expenses")
  @DeleteMapping(Endpoints.CATEGORY_BY_ID)
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    categoryService.delete(id);
    return ResponseEntity.noContent().build();
  }
}

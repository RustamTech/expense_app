package com.example.notes.application.coolproject.service.category;

import com.example.notes.application.coolproject.dto.category.CategoryCreateRequest;
import com.example.notes.application.coolproject.dto.category.CategoryResponse;
import com.example.notes.application.coolproject.dto.category.CategoryUpdateRequest;
import com.example.notes.application.coolproject.exception.custom.CategoryInUseException;
import com.example.notes.application.coolproject.exception.custom.CategoryNotFoundException;
import com.example.notes.application.coolproject.exception.custom.ResourceAlreadyExistsException;
import java.util.List;

/**
 * The shared category catalog. Reads are open to every authenticated user; writes are restricted to
 * administrators by {@code SecurityConfig}.
 */
public interface CategoryService {

  /**
   * Returns one category.
   *
   * @param id category identifier
   * @return the category
   * @throws CategoryNotFoundException if no category has this id
   */
  CategoryResponse findById(Long id);

  /**
   * Returns the whole catalog sorted by name.
   *
   * @return all categories
   */
  List<CategoryResponse> findAll();

  /**
   * Adds a category to the catalog.
   *
   * @param request create payload
   * @return the created category
   * @throws ResourceAlreadyExistsException if the name is taken (case-insensitive)
   */
  CategoryResponse create(CategoryCreateRequest request);

  /**
   * Changes the non-null fields of a category.
   *
   * @param id category identifier
   * @param request partial update payload
   * @return the updated category
   * @throws CategoryNotFoundException if no category has this id
   * @throws ResourceAlreadyExistsException if the new name is taken by another category
   */
  CategoryResponse update(Long id, CategoryUpdateRequest request);

  /**
   * Removes a category that no expense uses.
   *
   * @param id category identifier
   * @throws CategoryNotFoundException if no category has this id
   * @throws CategoryInUseException if any expense still references the category
   */
  void delete(Long id);
}

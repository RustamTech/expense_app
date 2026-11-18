package com.example.notes.application.coolproject.service.category;

import com.example.notes.application.coolproject.dto.category.CategoryCreateRequest;
import com.example.notes.application.coolproject.dto.category.CategoryResponse;
import com.example.notes.application.coolproject.dto.category.CategoryUpdateRequest;
import java.util.List;

public interface CategoryService {
  CategoryResponse findById(Long id);

  List<CategoryResponse> findAll();

  CategoryResponse create(CategoryCreateRequest request);

  CategoryResponse update(Long id, CategoryUpdateRequest request);

  void delete(Long id);
}

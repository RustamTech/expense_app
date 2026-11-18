package com.example.notes.application.coolproject.service.category;

import com.example.notes.application.coolproject.dto.category.CategoryCreateRequest;
import com.example.notes.application.coolproject.dto.category.CategoryResponse;
import com.example.notes.application.coolproject.dto.category.CategoryUpdateRequest;
import com.example.notes.application.coolproject.entity.CategoryEntity;
import com.example.notes.application.coolproject.exception.custom.CategoryInUseException;
import com.example.notes.application.coolproject.exception.custom.CategoryNotFoundException;
import com.example.notes.application.coolproject.exception.custom.ResourceAlreadyExistsException;
import com.example.notes.application.coolproject.mapper.CategoryMapper;
import com.example.notes.application.coolproject.repository.CategoryRepository;
import com.example.notes.application.coolproject.repository.ExpenseRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@inheritDoc} */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

  private final CategoryRepository categoryRepository;
  private final ExpenseRepository expenseRepository;
  private final CategoryMapper categoryMapper;

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public CategoryResponse findById(Long id) {
    return categoryMapper.toResponse(findCategory(id));
  }

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public List<CategoryResponse> findAll() {
    return categoryRepository.findAllByOrderByNameAsc().stream()
        .map(categoryMapper::toResponse)
        .toList();
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public CategoryResponse create(CategoryCreateRequest request) {
    String name = request.name().trim();
    if (categoryRepository.existsByNameIgnoreCase(name)) {
      throw nameTaken(name);
    }
    CategoryEntity entity = categoryMapper.toEntity(request);
    entity.setName(name);
    CategoryEntity saved = categoryRepository.save(entity);
    log.info("Created category with id {}", saved.getId());
    return categoryMapper.toResponse(saved);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public CategoryResponse update(Long id, CategoryUpdateRequest request) {
    CategoryEntity category = findCategory(id);
    if (request.name() != null
        && categoryRepository.existsByNameIgnoreCaseAndIdNot(request.name().trim(), id)) {
      throw nameTaken(request.name().trim());
    }
    categoryMapper.updateCategory(request, category);
    if (request.name() != null) {
      category.setName(request.name().trim());
    }
    log.info("Updated category with id {}", id);
    return categoryMapper.toResponse(category);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public void delete(Long id) {
    CategoryEntity category = findCategory(id);
    if (expenseRepository.existsByCategory_Id(id)) {
      throw new CategoryInUseException(id);
    }
    categoryRepository.delete(category);
    log.info("Deleted category with id {}", id);
  }

  private CategoryEntity findCategory(Long id) {
    return categoryRepository.findById(id).orElseThrow(() -> new CategoryNotFoundException(id));
  }

  private static ResourceAlreadyExistsException nameTaken(String name) {
    return new ResourceAlreadyExistsException("Category '" + name + "' already exists");
  }
}

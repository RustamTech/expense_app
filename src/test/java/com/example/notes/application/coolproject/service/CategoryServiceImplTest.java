package com.example.notes.application.coolproject.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.notes.application.coolproject.dto.category.CategoryCreateRequest;
import com.example.notes.application.coolproject.dto.category.CategoryResponse;
import com.example.notes.application.coolproject.dto.category.CategoryUpdateRequest;
import com.example.notes.application.coolproject.entity.CategoryEntity;
import com.example.notes.application.coolproject.enums.CategoryType;
import com.example.notes.application.coolproject.exception.custom.CategoryNotFoundException;
import com.example.notes.application.coolproject.exception.custom.ResourceAlreadyExistsException;
import com.example.notes.application.coolproject.mapper.CategoryMapper;
import com.example.notes.application.coolproject.repository.CategoryRepository;
import com.example.notes.application.coolproject.service.category.CategoryServiceImpl;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

  @Mock private CategoryRepository categoryRepository;

  @Mock private CategoryMapper categoryMapper;

  @InjectMocks private CategoryServiceImpl categoryService;

  @Test
  void findById_existingId_returnsResponse() {
    CategoryEntity entity = buildEntity(1L, "Food");
    CategoryResponse response = buildResponse(1L, "Food");

    when(categoryRepository.findById(1L)).thenReturn(Optional.of(entity));
    when(categoryMapper.toResponse(entity)).thenReturn(response);

    CategoryResponse result = categoryService.findById(1L);

    assertThat(result).isEqualTo(response);
    verify(categoryRepository).findById(1L);
  }

  @Test
  void findById_notFound_throwsCategoryNotFoundException() {
    when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> categoryService.findById(99L))
        .isInstanceOf(CategoryNotFoundException.class);
  }

  @Test
  void create_newName_returnsSavedResponse() {
    CategoryCreateRequest request =
        new CategoryCreateRequest("🍔", "red", "Food", CategoryType.EXPENSE);
    CategoryEntity entity = buildEntity(1L, "Food");
    CategoryResponse response = buildResponse(1L, "Food");

    when(categoryRepository.findByName("Food")).thenReturn(Optional.empty());
    when(categoryMapper.toEntity(request)).thenReturn(entity);
    when(categoryRepository.save(entity)).thenReturn(entity);
    when(categoryMapper.toResponse(entity)).thenReturn(response);

    CategoryResponse result = categoryService.create(request);

    assertThat(result).isEqualTo(response);
    verify(categoryRepository).save(entity);
  }

  @Test
  void create_duplicateName_throwsResourceAlreadyExistsException() {
    CategoryCreateRequest request =
        new CategoryCreateRequest("🍔", "red", "Food", CategoryType.EXPENSE);

    when(categoryRepository.findByName("Food")).thenReturn(Optional.of(buildEntity(1L, "Food")));

    assertThatThrownBy(() -> categoryService.create(request))
        .isInstanceOf(ResourceAlreadyExistsException.class);

    verify(categoryRepository, never()).save(any());
  }

  @Test
  void update_existingId_returnsUpdatedResponse() {
    CategoryUpdateRequest request =
        new CategoryUpdateRequest("🍕", "blue", "Pizza", CategoryType.EXPENSE);
    CategoryEntity entity = buildEntity(1L, "Food");
    CategoryResponse response = buildResponse(1L, "Pizza");

    when(categoryRepository.findById(1L)).thenReturn(Optional.of(entity));
    when(categoryRepository.save(entity)).thenReturn(entity);
    when(categoryMapper.toResponse(entity)).thenReturn(response);

    CategoryResponse result = categoryService.update(1L, request);

    assertThat(result).isEqualTo(response);
    verify(categoryMapper).updateCategory(request, entity);
  }

  @Test
  void update_notFound_throwsCategoryNotFoundException() {
    CategoryUpdateRequest request =
        new CategoryUpdateRequest("🍕", "blue", "Pizza", CategoryType.EXPENSE);
    when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> categoryService.update(99L, request))
        .isInstanceOf(CategoryNotFoundException.class);
  }

  @Test
  void delete_existingId_deletesSuccessfully() {
    CategoryEntity entity = buildEntity(1L, "Food");
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(entity));

    categoryService.delete(1L);

    verify(categoryRepository).delete(entity);
  }

  @Test
  void delete_notFound_throwsCategoryNotFoundException() {
    when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> categoryService.delete(99L))
        .isInstanceOf(CategoryNotFoundException.class);

    verify(categoryRepository, never()).delete(any());
  }

  @Test
  void findAll_returnsMappedList() {
    CategoryEntity entity = buildEntity(1L, "Food");
    CategoryResponse response = buildResponse(1L, "Food");

    when(categoryRepository.findAll()).thenReturn(List.of(entity));
    when(categoryMapper.toResponse(entity)).thenReturn(response);

    List<CategoryResponse> result = categoryService.findAll();

    assertThat(result).hasSize(1).containsExactly(response);
  }

  private CategoryEntity buildEntity(Long id, String name) {
    CategoryEntity e = new CategoryEntity();
    e.setName(name);
    e.setIcon("🍔");
    e.setColor("red");
    e.setCategoryType(CategoryType.EXPENSE);
    return e;
  }

  private CategoryResponse buildResponse(Long id, String name) {
    return new CategoryResponse(id, "red", "🍔", name);
  }
}

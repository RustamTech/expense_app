package com.example.notes.application.coolproject.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.notes.application.coolproject.TestData;
import com.example.notes.application.coolproject.dto.category.CategoryCreateRequest;
import com.example.notes.application.coolproject.dto.category.CategoryResponse;
import com.example.notes.application.coolproject.dto.category.CategoryUpdateRequest;
import com.example.notes.application.coolproject.entity.CategoryEntity;
import com.example.notes.application.coolproject.enums.CategoryType;
import com.example.notes.application.coolproject.exception.custom.CategoryInUseException;
import com.example.notes.application.coolproject.exception.custom.CategoryNotFoundException;
import com.example.notes.application.coolproject.exception.custom.ResourceAlreadyExistsException;
import com.example.notes.application.coolproject.mapper.CategoryMapper;
import com.example.notes.application.coolproject.repository.CategoryRepository;
import com.example.notes.application.coolproject.repository.ExpenseRepository;
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
  @Mock private ExpenseRepository expenseRepository;
  @Mock private CategoryMapper categoryMapper;

  @InjectMocks private CategoryServiceImpl categoryService;

  private final CategoryEntity food = TestData.category(1L, "Food");
  private final CategoryResponse foodResponse = TestData.categoryResponse(1L, "Food");

  @Test
  void returnsCategoryById() {
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(food));
    when(categoryMapper.toResponse(food)).thenReturn(foodResponse);

    assertThat(categoryService.findById(1L)).isEqualTo(foodResponse);
  }

  @Test
  void missingCategoryThrowsNotFound() {
    when(categoryRepository.findById(9L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> categoryService.findById(9L))
        .isInstanceOf(CategoryNotFoundException.class);
  }

  @Test
  void listsCatalogSortedByName() {
    when(categoryRepository.findAllByOrderByNameAsc()).thenReturn(List.of(food));
    when(categoryMapper.toResponse(food)).thenReturn(foodResponse);

    assertThat(categoryService.findAll()).containsExactly(foodResponse);
  }

  @Test
  void createsCategoryWithTrimmedName() {
    var request = new CategoryCreateRequest("🍔", "#FF0000", "  Food ", CategoryType.EXPENSE);
    CategoryEntity mapped = new CategoryEntity();
    when(categoryRepository.existsByNameIgnoreCase("Food")).thenReturn(false);
    when(categoryMapper.toEntity(request)).thenReturn(mapped);
    when(categoryRepository.save(mapped)).thenReturn(food);
    when(categoryMapper.toResponse(food)).thenReturn(foodResponse);

    assertThat(categoryService.create(request)).isEqualTo(foodResponse);
    assertThat(mapped.getName()).isEqualTo("Food");
  }

  @Test
  void createWithTakenNameThrowsConflict() {
    var request = new CategoryCreateRequest("🍔", "#FF0000", "food", CategoryType.EXPENSE);
    when(categoryRepository.existsByNameIgnoreCase("food")).thenReturn(true);

    assertThatThrownBy(() -> categoryService.create(request))
        .isInstanceOf(ResourceAlreadyExistsException.class);
    verify(categoryRepository, never()).save(any());
  }

  @Test
  void updatesCategory() {
    var request = new CategoryUpdateRequest(null, "#00FF00", "Meals", null);
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(food));
    when(categoryRepository.existsByNameIgnoreCaseAndIdNot("Meals", 1L)).thenReturn(false);

    categoryService.update(1L, request);

    verify(categoryMapper).updateCategory(request, food);
    assertThat(food.getName()).isEqualTo("Meals");
  }

  @Test
  void renameToNameOfAnotherCategoryThrowsConflict() {
    var request = new CategoryUpdateRequest(null, null, "Taxi", null);
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(food));
    when(categoryRepository.existsByNameIgnoreCaseAndIdNot("Taxi", 1L)).thenReturn(true);

    assertThatThrownBy(() -> categoryService.update(1L, request))
        .isInstanceOf(ResourceAlreadyExistsException.class);
    verify(categoryMapper, never()).updateCategory(any(), any());
  }

  @Test
  void updateWithoutNameSkipsUniquenessCheck() {
    var request = new CategoryUpdateRequest("🍕", null, null, null);
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(food));

    categoryService.update(1L, request);

    verify(categoryRepository, never()).existsByNameIgnoreCaseAndIdNot(any(), any());
    assertThat(food.getName()).isEqualTo("Food");
  }

  @Test
  void updateOfMissingCategoryThrowsNotFound() {
    when(categoryRepository.findById(9L)).thenReturn(Optional.empty());
    var request = new CategoryUpdateRequest(null, null, "X", null);

    assertThatThrownBy(() -> categoryService.update(9L, request))
        .isInstanceOf(CategoryNotFoundException.class);
  }

  @Test
  void deletesUnusedCategory() {
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(food));
    when(expenseRepository.existsByCategory_Id(1L)).thenReturn(false);

    categoryService.delete(1L);

    verify(categoryRepository).delete(food);
  }

  @Test
  void deletingCategoryInUseThrowsConflict() {
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(food));
    when(expenseRepository.existsByCategory_Id(1L)).thenReturn(true);

    assertThatThrownBy(() -> categoryService.delete(1L)).isInstanceOf(CategoryInUseException.class);
    verify(categoryRepository, never()).delete(any());
  }

  @Test
  void deletingMissingCategoryThrowsNotFound() {
    when(categoryRepository.findById(9L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> categoryService.delete(9L))
        .isInstanceOf(CategoryNotFoundException.class);
  }
}

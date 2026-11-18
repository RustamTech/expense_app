package com.example.notes.application.coolproject.mapper;

import com.example.notes.application.coolproject.dto.category.CategoryCreateRequest;
import com.example.notes.application.coolproject.dto.category.CategoryResponse;
import com.example.notes.application.coolproject.dto.category.CategoryUpdateRequest;
import com.example.notes.application.coolproject.entity.CategoryEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** Maps catalog categories between entities and DTOs. */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

  /**
   * Creates a new category from a create request.
   *
   * @param request create payload
   * @return new entity
   */
  @Mapping(target = "defaultCategory", ignore = true)
  CategoryEntity toEntity(CategoryCreateRequest request);

  /**
   * Copies the non-null fields of an update request onto an existing category.
   *
   * @param request partial update payload
   * @param entity category to modify
   */
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "defaultCategory", ignore = true)
  void updateCategory(CategoryUpdateRequest request, @MappingTarget CategoryEntity entity);

  /**
   * Converts a category to its response.
   *
   * @param entity the category
   * @return response DTO
   */
  CategoryResponse toResponse(CategoryEntity entity);
}

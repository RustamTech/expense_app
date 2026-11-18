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

@Mapper(componentModel = "spring")
public interface CategoryMapper {

  @Mapping(target = "defaultCategory", ignore = true)
  CategoryEntity toEntity(CategoryCreateRequest request);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "defaultCategory", ignore = true)
  void updateCategory(CategoryUpdateRequest request, @MappingTarget CategoryEntity entity);

  CategoryResponse toResponse(CategoryEntity entity);
}

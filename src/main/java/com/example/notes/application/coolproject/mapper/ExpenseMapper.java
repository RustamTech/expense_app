package com.example.notes.application.coolproject.mapper;

import com.example.notes.application.coolproject.dto.expense.ExpenseCreateRequest;
import com.example.notes.application.coolproject.dto.expense.ExpenseResponse;
import com.example.notes.application.coolproject.dto.expense.ExpenseUpdateRequest;
import com.example.notes.application.coolproject.entity.ExpenseEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** Maps expenses between entities and DTOs. Owner and category are always set by the service. */
@Mapper(componentModel = "spring", uses = CategoryMapper.class)
public interface ExpenseMapper {

  /**
   * Creates a new, unowned expense from a create request.
   *
   * @param request create payload
   * @return entity without owner and category
   */
  @Mapping(target = "category", ignore = true)
  @Mapping(target = "user", ignore = true)
  ExpenseEntity toEntity(ExpenseCreateRequest request);

  /**
   * Copies the non-null fields of an update request onto an existing expense.
   *
   * @param request partial update payload
   * @param entity expense to modify
   */
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "category", ignore = true)
  @Mapping(target = "user", ignore = true)
  void updateEntity(ExpenseUpdateRequest request, @MappingTarget ExpenseEntity entity);

  /**
   * Converts an expense to its response.
   *
   * @param entity the expense
   * @return response DTO
   */
  ExpenseResponse toResponse(ExpenseEntity entity);
}

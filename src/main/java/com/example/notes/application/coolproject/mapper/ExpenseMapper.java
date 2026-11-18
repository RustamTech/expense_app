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

@Mapper(componentModel = "spring")
public interface ExpenseMapper {

  @Mapping(target = "category", ignore = true)
  @Mapping(target = "user", ignore = true)
  ExpenseEntity toEntity(ExpenseCreateRequest request);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "category", ignore = true)
  void updateEntity(ExpenseUpdateRequest request, @MappingTarget ExpenseEntity entity);

  ExpenseResponse toResponse(ExpenseEntity entity);
}

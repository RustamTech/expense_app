package com.example.notes.application.coolproject.mapper;

import com.example.notes.application.coolproject.dto.user.UserResponse;
import com.example.notes.application.coolproject.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
  UserResponse toResponse(UserEntity user);
}

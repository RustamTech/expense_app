package com.example.notes.application.coolproject.mapper;

import com.example.notes.application.coolproject.dto.user.UserResponse;
import com.example.notes.application.coolproject.entity.UserEntity;
import org.mapstruct.Mapper;

/** Maps accounts to their public view. */
@Mapper(componentModel = "spring")
public interface UserMapper {

  /**
   * Converts an account to its public view (without the password hash).
   *
   * @param user the account
   * @return response DTO
   */
  UserResponse toResponse(UserEntity user);
}

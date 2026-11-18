package com.example.notes.application.coolproject.service.user;

import com.example.notes.application.coolproject.dto.user.UserResponse;
import com.example.notes.application.coolproject.dto.user.UserUpdateRequest;
import com.example.notes.application.coolproject.entity.UserEntity;
import com.example.notes.application.coolproject.exception.custom.UserNotFoundException;
import com.example.notes.application.coolproject.mapper.UserMapper;
import com.example.notes.application.coolproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@inheritDoc} */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public UserResponse getCurrentUser(String email) {
    return userMapper.toResponse(findUser(email));
  }

  /** {@inheritDoc} Changes are flushed by the transaction; no explicit save is needed. */
  @Override
  @Transactional
  public UserResponse updateCurrentUser(String email, UserUpdateRequest request) {
    UserEntity user = findUser(email);
    user.setFirstName(request.firstName().trim());
    user.setLastName(request.lastName().trim());
    user.setPhoneNumber(request.phoneNumber());
    log.info("Updated profile of user with id {}", user.getId());
    return userMapper.toResponse(user);
  }

  private UserEntity findUser(String email) {
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> UserNotFoundException.byEmail(email));
  }
}

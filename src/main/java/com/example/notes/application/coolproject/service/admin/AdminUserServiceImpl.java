package com.example.notes.application.coolproject.service.admin;

import com.example.notes.application.coolproject.dto.PageResponse;
import com.example.notes.application.coolproject.dto.user.UserResponse;
import com.example.notes.application.coolproject.entity.UserEntity;
import com.example.notes.application.coolproject.enums.UserRole;
import com.example.notes.application.coolproject.exception.custom.OperationNotAllowedException;
import com.example.notes.application.coolproject.exception.custom.UserNotFoundException;
import com.example.notes.application.coolproject.mapper.UserMapper;
import com.example.notes.application.coolproject.repository.ExpenseRepository;
import com.example.notes.application.coolproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@inheritDoc} */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

  private final UserRepository userRepository;
  private final ExpenseRepository expenseRepository;
  private final UserMapper userMapper;

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public PageResponse<UserResponse> findAll(Pageable pageable) {
    return PageResponse.from(userRepository.findAll(pageable).map(userMapper::toResponse));
  }

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public UserResponse findById(Long id) {
    return userMapper.toResponse(findUser(id));
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public UserResponse changeRole(String actorEmail, Long id, UserRole role) {
    UserEntity user = findOtherUser(actorEmail, id, "change your own role");
    user.setUserRole(role);
    log.info("User with id {} now has role {}", id, role);
    return userMapper.toResponse(user);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public UserResponse changeStatus(String actorEmail, Long id, boolean enabled) {
    UserEntity user = findOtherUser(actorEmail, id, "block or unblock your own account");
    user.setEnabled(enabled);
    log.info("User with id {} is now {}", id, enabled ? "enabled" : "blocked");
    return userMapper.toResponse(user);
  }

  /** {@inheritDoc} Expenses are removed first because they reference the account. */
  @Override
  @Transactional
  public void delete(String actorEmail, Long id) {
    UserEntity user = findOtherUser(actorEmail, id, "delete your own account");
    int deletedExpenses = expenseRepository.deleteAllByUserId(id);
    userRepository.delete(user);
    log.info("Deleted user with id {} and {} expenses", id, deletedExpenses);
  }

  private UserEntity findUser(Long id) {
    return userRepository.findById(id).orElseThrow(() -> UserNotFoundException.byId(id));
  }

  private UserEntity findOtherUser(String actorEmail, Long id, String forbiddenAction) {
    UserEntity user = findUser(id);
    if (user.getEmail().equals(actorEmail)) {
      throw new OperationNotAllowedException("You cannot " + forbiddenAction);
    }
    return user;
  }
}

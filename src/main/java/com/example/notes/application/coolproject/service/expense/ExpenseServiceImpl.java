package com.example.notes.application.coolproject.service.expense;

import com.example.notes.application.coolproject.dto.expense.ExpenseCreateRequest;
import com.example.notes.application.coolproject.dto.expense.ExpenseResponse;
import com.example.notes.application.coolproject.dto.expense.ExpenseUpdateRequest;
import com.example.notes.application.coolproject.entity.CategoryEntity;
import com.example.notes.application.coolproject.entity.ExpenseEntity;
import com.example.notes.application.coolproject.entity.UserEntity;
import com.example.notes.application.coolproject.enums.StatusType;
import com.example.notes.application.coolproject.exception.custom.CategoryNotFoundException;
import com.example.notes.application.coolproject.exception.custom.ExpenseNotFoundException;
import com.example.notes.application.coolproject.exception.custom.UserNotFoundException;
import com.example.notes.application.coolproject.mapper.ExpenseMapper;
import com.example.notes.application.coolproject.repository.CategoryRepository;
import com.example.notes.application.coolproject.repository.ExpenseRepository;
import com.example.notes.application.coolproject.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@inheritDoc} */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

  private final ExpenseMapper expenseMapper;
  private final ExpenseRepository expenseRepository;
  private final CategoryRepository categoryRepository;
  private final UserRepository userRepository;

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public ExpenseResponse findById(String email, Long id) {
    return expenseMapper.toResponse(findOwnExpense(findUser(email), id));
  }

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public List<ExpenseResponse> findAll(String email) {
    return toResponses(expenseRepository.findAllByUser_IdOrderByDateDesc(findUser(email).getId()));
  }

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public List<ExpenseResponse> findAllByStatus(String email, StatusType statusType) {
    Long userId = findUser(email).getId();
    return toResponses(
        expenseRepository.findAllByUser_IdAndStatusTypeOrderByDateDesc(userId, statusType));
  }

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public List<ExpenseResponse> findAllByCategory(String email, Long categoryId) {
    Long userId = findUser(email).getId();
    if (!categoryRepository.existsById(categoryId)) {
      throw new CategoryNotFoundException(categoryId);
    }
    return toResponses(
        expenseRepository.findAllByUser_IdAndCategory_IdOrderByDateDesc(userId, categoryId));
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public ExpenseResponse create(String email, ExpenseCreateRequest request) {
    UserEntity user = findUser(email);
    ExpenseEntity expense = expenseMapper.toEntity(request);
    expense.setCategory(findCategory(request.categoryId()));
    expense.setUser(user);
    ExpenseEntity saved = expenseRepository.save(expense);
    log.info("User {} created expense {}", user.getId(), saved.getId());
    return expenseMapper.toResponse(saved);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public ExpenseResponse update(String email, Long id, ExpenseUpdateRequest request) {
    ExpenseEntity expense = findOwnExpense(findUser(email), id);
    if (request.categoryId() != null) {
      expense.setCategory(findCategory(request.categoryId()));
    }
    expenseMapper.updateEntity(request, expense);
    ExpenseEntity updated = expenseRepository.save(expense);
    log.info("Updated expense {}", id);
    return expenseMapper.toResponse(updated);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public void delete(String email, Long id) {
    expenseRepository.delete(findOwnExpense(findUser(email), id));
    log.info("Deleted expense {}", id);
  }

  private UserEntity findUser(String email) {
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> UserNotFoundException.byEmail(email));
  }

  private ExpenseEntity findOwnExpense(UserEntity owner, Long id) {
    return expenseRepository
        .findByIdAndUser_Id(id, owner.getId())
        .orElseThrow(() -> new ExpenseNotFoundException(id));
  }

  private CategoryEntity findCategory(Long categoryId) {
    return categoryRepository
        .findById(categoryId)
        .orElseThrow(() -> new CategoryNotFoundException(categoryId));
  }

  private List<ExpenseResponse> toResponses(List<ExpenseEntity> expenses) {
    return expenses.stream().map(expenseMapper::toResponse).toList();
  }
}

package com.example.notes.application.coolproject.service.expense;

import com.example.notes.application.coolproject.dto.expenseDto.ExpenseCreateRequest;
import com.example.notes.application.coolproject.dto.expenseDto.ExpenseResponse;
import com.example.notes.application.coolproject.dto.expenseDto.ExpenseUpdateRequest;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseServiceImpl implements ExpenseService {

  private final ExpenseMapper expenseMapper;
  private final ExpenseRepository expenseRepository;
  private final CategoryRepository categoryRepository;
  private final UserRepository userRepository;

  private UserEntity getUser(String email) {
    return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
  }

  @Override
  @Transactional(readOnly = true)
  public ExpenseResponse findById(String email, Long id) {
    log.info("Fetching expense by id: {}", id);
    UserEntity user = getUser(email);
    ExpenseEntity expense =
        expenseRepository
            .findByIdAndUser_Id(id, user.getId())
            .orElseThrow(
                () -> {
                  log.error("Expense not found with this id: {}", id);
                  return new ExpenseNotFoundException(id);
                });
    log.info("Successfully fetched expense with id: {}", id);
    return expenseMapper.toResponse(expense);
  }

  @Override
  @Transactional
  public void delete(String email, Long id) {
    log.info("Delete expense by id: {}", id);
    UserEntity user = getUser(email);
    ExpenseEntity expense =
        expenseRepository
            .findByIdAndUser_Id(id, user.getId())
            .orElseThrow(
                () -> {
                  log.error("Expense already deleted by id: {}", id);
                  return new ExpenseNotFoundException(id);
                });
    expenseRepository.delete(expense);
    log.info("Successfully deleted expense by id: {}", id);
  }

  @Override
  @Transactional
  public ExpenseResponse create(String email, ExpenseCreateRequest request) {
    log.info("Creating expense with description: {}", request.description());
    UserEntity user = getUser(email);
    CategoryEntity category =
        categoryRepository
            .findById(request.categoryId())
            .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));
    ExpenseEntity expense = expenseMapper.toEntity(request);
    expense.setCategory(category);
    expense.setUser(user);
    ExpenseEntity saved = expenseRepository.save(expense);
    log.info("Successfully created expense with id: {}", saved.getId());
    return expenseMapper.toResponse(saved);
  }

  @Override
  @Transactional
  public ExpenseResponse update(String email, Long id, ExpenseUpdateRequest request) {
    log.info("Updating expense with id: {}", id);
    UserEntity user = getUser(email);
    ExpenseEntity expense =
        expenseRepository
            .findByIdAndUser_Id(id, user.getId())
            .orElseThrow(() -> new ExpenseNotFoundException(id));
    if (request.categoryId() != null) {
      CategoryEntity category =
          categoryRepository
              .findById(request.categoryId())
              .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));
      expense.setCategory(category);
    }
    expenseMapper.updateEntity(request, expense);
    ExpenseEntity updated = expenseRepository.save(expense);
    log.info("Successfully updated expense with id: {}", id);
    return expenseMapper.toResponse(updated);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ExpenseResponse> findAll(String email) {
    log.info("Fetching all expenses");
    UserEntity user = getUser(email);
    List<ExpenseResponse> expenses =
        expenseRepository.findAllByUser_Id(user.getId()).stream()
            .map(expenseMapper::toResponse)
            .toList();
    log.info("Successfully fetched all expenses");
    return expenses;
  }

  @Override
  @Transactional(readOnly = true)
  public List<ExpenseResponse> findAllByStatus(String email, StatusType statusType) {
    log.info("Fetching all expenses by status: {}", statusType);
    UserEntity user = getUser(email);
    List<ExpenseResponse> expenses =
        expenseRepository.findAllByUser_IdAndStatusType(user.getId(), statusType).stream()
            .map(expenseMapper::toResponse)
            .toList();
    log.info("Successfully fetched all expenses by status: {}", statusType);
    return expenses;
  }

  @Override
  @Transactional(readOnly = true)
  public List<ExpenseResponse> findAllByCategory(String email, Long categoryId) {
    log.info("Fetching all expenses by category: {}", categoryId);
    UserEntity user = getUser(email);
    categoryRepository
        .findById(categoryId)
        .orElseThrow(() -> new CategoryNotFoundException(categoryId));
    return expenseRepository.findAllByUser_IdAndCategory_Id(user.getId(), categoryId).stream()
        .map(expenseMapper::toResponse)
        .toList();
  }
}

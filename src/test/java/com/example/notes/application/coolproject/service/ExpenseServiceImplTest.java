package com.example.notes.application.coolproject.service;

import static com.example.notes.application.coolproject.TestData.USER_EMAIL;
import static com.example.notes.application.coolproject.TestData.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.notes.application.coolproject.TestData;
import com.example.notes.application.coolproject.dto.expense.ExpenseCreateRequest;
import com.example.notes.application.coolproject.dto.expense.ExpenseResponse;
import com.example.notes.application.coolproject.dto.expense.ExpenseUpdateRequest;
import com.example.notes.application.coolproject.entity.CategoryEntity;
import com.example.notes.application.coolproject.entity.ExpenseEntity;
import com.example.notes.application.coolproject.entity.UserEntity;
import com.example.notes.application.coolproject.enums.CurrencyType;
import com.example.notes.application.coolproject.enums.StatusType;
import com.example.notes.application.coolproject.exception.custom.CategoryNotFoundException;
import com.example.notes.application.coolproject.exception.custom.ExpenseNotFoundException;
import com.example.notes.application.coolproject.exception.custom.UserNotFoundException;
import com.example.notes.application.coolproject.mapper.ExpenseMapper;
import com.example.notes.application.coolproject.repository.CategoryRepository;
import com.example.notes.application.coolproject.repository.ExpenseRepository;
import com.example.notes.application.coolproject.repository.UserRepository;
import com.example.notes.application.coolproject.service.expense.ExpenseServiceImpl;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceImplTest {

  @Mock private ExpenseMapper expenseMapper;
  @Mock private ExpenseRepository expenseRepository;
  @Mock private CategoryRepository categoryRepository;
  @Mock private UserRepository userRepository;

  @InjectMocks private ExpenseServiceImpl expenseService;

  private final UserEntity owner = TestData.regularUser();
  private final CategoryEntity food = TestData.category(1L, "Food");
  private final ExpenseEntity expense =
      TestData.expense(10L, "12.50", CurrencyType.AZN, StatusType.PAID, food);
  private final ExpenseResponse response = TestData.expenseResponse(10L);

  private void ownerExists() {
    when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(owner));
  }

  @Test
  void unknownCallerThrowsUserNotFound() {
    when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> expenseService.findAll(USER_EMAIL))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void findsOwnExpenseScopedToOwner() {
    ownerExists();
    when(expenseRepository.findByIdAndUser_Id(10L, USER_ID)).thenReturn(Optional.of(expense));
    when(expenseMapper.toResponse(expense)).thenReturn(response);

    assertThat(expenseService.findById(USER_EMAIL, 10L)).isEqualTo(response);
  }

  @Test
  void foreignExpenseLooksMissing() {
    ownerExists();
    when(expenseRepository.findByIdAndUser_Id(77L, USER_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> expenseService.findById(USER_EMAIL, 77L))
        .isInstanceOf(ExpenseNotFoundException.class);
  }

  @Test
  void listsOnlyOwnExpenses() {
    ownerExists();
    when(expenseRepository.findAllByUser_IdOrderByDateDesc(USER_ID)).thenReturn(List.of(expense));
    when(expenseMapper.toResponse(expense)).thenReturn(response);

    assertThat(expenseService.findAll(USER_EMAIL)).containsExactly(response);
  }

  @Test
  void filtersOwnExpensesByStatus() {
    ownerExists();
    when(expenseRepository.findAllByUser_IdAndStatusTypeOrderByDateDesc(USER_ID, StatusType.PAID))
        .thenReturn(List.of(expense));
    when(expenseMapper.toResponse(expense)).thenReturn(response);

    assertThat(expenseService.findAllByStatus(USER_EMAIL, StatusType.PAID)).hasSize(1);
  }

  @Test
  void filtersOwnExpensesByCategory() {
    ownerExists();
    when(categoryRepository.existsById(1L)).thenReturn(true);
    when(expenseRepository.findAllByUser_IdAndCategory_IdOrderByDateDesc(USER_ID, 1L))
        .thenReturn(List.of(expense));
    when(expenseMapper.toResponse(expense)).thenReturn(response);

    assertThat(expenseService.findAllByCategory(USER_EMAIL, 1L)).containsExactly(response);
  }

  @Test
  void filterByMissingCategoryThrowsNotFound() {
    ownerExists();
    when(categoryRepository.existsById(9L)).thenReturn(false);

    assertThatThrownBy(() -> expenseService.findAllByCategory(USER_EMAIL, 9L))
        .isInstanceOf(CategoryNotFoundException.class);
  }

  @Test
  void createAssignsCallerAsOwnerAndCategory() {
    ownerExists();
    var request = createRequest(1L);
    ExpenseEntity mapped = new ExpenseEntity();
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(food));
    when(expenseMapper.toEntity(request)).thenReturn(mapped);
    when(expenseRepository.save(mapped)).thenReturn(mapped);
    when(expenseMapper.toResponse(mapped)).thenReturn(response);

    assertThat(expenseService.create(USER_EMAIL, request)).isEqualTo(response);
    assertThat(mapped.getUser()).isSameAs(owner);
    assertThat(mapped.getCategory()).isSameAs(food);
  }

  @Test
  void createWithMissingCategoryThrowsAndSavesNothing() {
    ownerExists();
    var request = createRequest(9L);
    when(expenseMapper.toEntity(request)).thenReturn(new ExpenseEntity());
    when(categoryRepository.findById(9L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> expenseService.create(USER_EMAIL, request))
        .isInstanceOf(CategoryNotFoundException.class);
    verify(expenseRepository, never()).save(any());
  }

  @Test
  void updateChangesCategoryWhenGiven() {
    ownerExists();
    CategoryEntity taxi = TestData.category(2L, "Taxi");
    var request = new ExpenseUpdateRequest(new BigDecimal("20"), null, null, null, null, 2L, null);
    when(expenseRepository.findByIdAndUser_Id(10L, USER_ID)).thenReturn(Optional.of(expense));
    when(categoryRepository.findById(2L)).thenReturn(Optional.of(taxi));
    when(expenseRepository.save(expense)).thenReturn(expense);
    when(expenseMapper.toResponse(expense)).thenReturn(response);

    expenseService.update(USER_EMAIL, 10L, request);

    assertThat(expense.getCategory()).isSameAs(taxi);
    verify(expenseMapper).updateEntity(request, expense);
  }

  @Test
  void updateWithoutCategoryKeepsCurrentOne() {
    ownerExists();
    var request = new ExpenseUpdateRequest(null, "Dinner", null, null, null, null, null);
    when(expenseRepository.findByIdAndUser_Id(10L, USER_ID)).thenReturn(Optional.of(expense));
    when(expenseRepository.save(expense)).thenReturn(expense);

    expenseService.update(USER_EMAIL, 10L, request);

    assertThat(expense.getCategory()).isSameAs(food);
    verify(categoryRepository, never()).findById(any());
  }

  @Test
  void updateOfForeignExpenseThrowsNotFound() {
    ownerExists();
    var request = new ExpenseUpdateRequest(null, "Dinner", null, null, null, null, null);
    when(expenseRepository.findByIdAndUser_Id(77L, USER_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> expenseService.update(USER_EMAIL, 77L, request))
        .isInstanceOf(ExpenseNotFoundException.class);
    verify(expenseRepository, never()).save(any());
  }

  @Test
  void updateWithMissingCategoryThrowsNotFound() {
    ownerExists();
    var request = new ExpenseUpdateRequest(null, null, null, null, null, 9L, null);
    when(expenseRepository.findByIdAndUser_Id(10L, USER_ID)).thenReturn(Optional.of(expense));
    when(categoryRepository.findById(9L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> expenseService.update(USER_EMAIL, 10L, request))
        .isInstanceOf(CategoryNotFoundException.class);
  }

  @Test
  void deletesOwnExpense() {
    ownerExists();
    when(expenseRepository.findByIdAndUser_Id(10L, USER_ID)).thenReturn(Optional.of(expense));

    expenseService.delete(USER_EMAIL, 10L);

    verify(expenseRepository).delete(expense);
  }

  @Test
  void deletingForeignExpenseThrowsAndDeletesNothing() {
    ownerExists();
    when(expenseRepository.findByIdAndUser_Id(77L, USER_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> expenseService.delete(USER_EMAIL, 77L))
        .isInstanceOf(ExpenseNotFoundException.class);
    verify(expenseRepository, never()).delete(any());
  }

  private static ExpenseCreateRequest createRequest(Long categoryId) {
    return new ExpenseCreateRequest(
        new BigDecimal("12.50"),
        "Lunch",
        null,
        CurrencyType.AZN,
        TestData.DAY,
        categoryId,
        StatusType.PAID);
  }
}

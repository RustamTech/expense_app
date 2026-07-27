package com.example.notes.application.coolproject.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.notes.application.coolproject.dto.expenseDto.ExpenseCreateRequest;
import com.example.notes.application.coolproject.dto.expenseDto.ExpenseResponse;
import com.example.notes.application.coolproject.dto.expenseDto.ExpenseUpdateRequest;
import com.example.notes.application.coolproject.entity.CategoryEntity;
import com.example.notes.application.coolproject.entity.ExpenseEntity;
import com.example.notes.application.coolproject.entity.UserEntity;
import com.example.notes.application.coolproject.enums.CategoryType;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceImplTest {

  private static final String EMAIL = "user@example.com";
  private static final Long USER_ID = 1L;

  @Mock private ExpenseMapper expenseMapper;

  @Mock private ExpenseRepository expenseRepository;

  @Mock private CategoryRepository categoryRepository;

  @Mock private UserRepository userRepository;

  @InjectMocks private ExpenseServiceImpl expenseService;

  private void mockUser() {
    when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(buildUserEntity()));
  }

  @Test
  void findById_exists_returnsResponse() {
    mockUser();
    ExpenseEntity entity = buildExpenseEntity();
    ExpenseResponse response = buildExpenseResponse(1L);

    when(expenseRepository.findByIdAndUser_Id(1L, USER_ID)).thenReturn(Optional.of(entity));
    when(expenseMapper.toResponse(entity)).thenReturn(response);

    ExpenseResponse result = expenseService.findById(EMAIL, 1L);

    assertThat(result).isEqualTo(response);
  }

  @Test
  void findById_notFound_throwsExpenseNotFoundException() {
    mockUser();
    when(expenseRepository.findByIdAndUser_Id(99L, USER_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> expenseService.findById(EMAIL, 99L))
        .isInstanceOf(ExpenseNotFoundException.class);
  }

  @Test
  void findById_userNotFound_throwsUserNotFoundException() {
    when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> expenseService.findById(EMAIL, 1L))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void create_validRequest_returnsSavedResponse() {
    mockUser();
    ExpenseCreateRequest request =
        new ExpenseCreateRequest(
            BigDecimal.TEN, "Lunch", null, CurrencyType.USD, LocalDate.now(), 1L, StatusType.PAID);
    CategoryEntity category = buildCategoryEntity(1L);
    ExpenseEntity entity = buildExpenseEntity();
    ExpenseResponse response = buildExpenseResponse(1L);

    when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
    when(expenseMapper.toEntity(request)).thenReturn(entity);
    when(expenseRepository.save(entity)).thenReturn(entity);
    when(expenseMapper.toResponse(entity)).thenReturn(response);

    ExpenseResponse result = expenseService.create(EMAIL, request);

    assertThat(result).isEqualTo(response);
    assertThat(entity.getUser()).isEqualTo(buildUserEntity());
    verify(expenseRepository).save(entity);
  }

  @Test
  void create_categoryNotFound_throwsCategoryNotFoundException() {
    mockUser();
    ExpenseCreateRequest request =
        new ExpenseCreateRequest(
            BigDecimal.TEN, "Lunch", null, CurrencyType.USD, LocalDate.now(), 99L, StatusType.PAID);

    when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> expenseService.create(EMAIL, request))
        .isInstanceOf(CategoryNotFoundException.class);

    verify(expenseRepository, never()).save(any());
  }

  @Test
  void update_existingExpense_returnsUpdatedResponse() {
    mockUser();
    ExpenseUpdateRequest request =
        new ExpenseUpdateRequest(
            BigDecimal.valueOf(20),
            "Dinner",
            null,
            CurrencyType.EUR,
            LocalDate.now(),
            null,
            StatusType.PLANNED);
    ExpenseEntity entity = buildExpenseEntity();
    ExpenseResponse response = buildExpenseResponse(1L);

    when(expenseRepository.findByIdAndUser_Id(1L, USER_ID)).thenReturn(Optional.of(entity));
    when(expenseRepository.save(entity)).thenReturn(entity);
    when(expenseMapper.toResponse(entity)).thenReturn(response);

    ExpenseResponse result = expenseService.update(EMAIL, 1L, request);

    assertThat(result).isEqualTo(response);
    verify(expenseMapper).updateEntity(request, entity);
  }

  @Test
  void update_notFound_throwsExpenseNotFoundException() {
    mockUser();
    ExpenseUpdateRequest request =
        new ExpenseUpdateRequest(
            BigDecimal.TEN,
            "Lunch",
            null,
            CurrencyType.USD,
            LocalDate.now(),
            null,
            StatusType.PAID);

    when(expenseRepository.findByIdAndUser_Id(99L, USER_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> expenseService.update(EMAIL, 99L, request))
        .isInstanceOf(ExpenseNotFoundException.class);
  }

  @Test
  void update_withNewCategory_loadsAndAssignsCategory() {
    mockUser();
    ExpenseUpdateRequest request =
        new ExpenseUpdateRequest(
            BigDecimal.TEN, "Lunch", null, CurrencyType.USD, LocalDate.now(), 2L, StatusType.PAID);
    ExpenseEntity entity = buildExpenseEntity();
    CategoryEntity newCategory = buildCategoryEntity(2L);
    ExpenseResponse response = buildExpenseResponse(1L);

    when(expenseRepository.findByIdAndUser_Id(1L, USER_ID)).thenReturn(Optional.of(entity));
    when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));
    when(expenseRepository.save(entity)).thenReturn(entity);
    when(expenseMapper.toResponse(entity)).thenReturn(response);

    expenseService.update(EMAIL, 1L, request);

    assertThat(entity.getCategory()).isEqualTo(newCategory);
  }

  @Test
  void update_newCategoryNotFound_throwsCategoryNotFoundException() {
    mockUser();
    ExpenseUpdateRequest request =
        new ExpenseUpdateRequest(
            BigDecimal.TEN, "Lunch", null, CurrencyType.USD, LocalDate.now(), 99L, StatusType.PAID);
    ExpenseEntity entity = buildExpenseEntity();

    when(expenseRepository.findByIdAndUser_Id(1L, USER_ID)).thenReturn(Optional.of(entity));
    when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> expenseService.update(EMAIL, 1L, request))
        .isInstanceOf(CategoryNotFoundException.class);
  }

  @Test
  void delete_existingExpense_deletesSuccessfully() {
    mockUser();
    ExpenseEntity entity = buildExpenseEntity();
    when(expenseRepository.findByIdAndUser_Id(1L, USER_ID)).thenReturn(Optional.of(entity));

    expenseService.delete(EMAIL, 1L);

    verify(expenseRepository).delete(entity);
  }

  @Test
  void delete_notFound_throwsExpenseNotFoundException() {
    mockUser();
    when(expenseRepository.findByIdAndUser_Id(99L, USER_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> expenseService.delete(EMAIL, 99L))
        .isInstanceOf(ExpenseNotFoundException.class);

    verify(expenseRepository, never()).delete(any());
  }

  @Test
  void findAll_returnsMappedList() {
    mockUser();
    ExpenseEntity entity = buildExpenseEntity();
    ExpenseResponse response = buildExpenseResponse(1L);

    when(expenseRepository.findAllByUser_Id(USER_ID)).thenReturn(List.of(entity));
    when(expenseMapper.toResponse(entity)).thenReturn(response);

    List<ExpenseResponse> result = expenseService.findAll(EMAIL);

    assertThat(result).hasSize(1).containsExactly(response);
  }

  @Test
  void findAllByStatus_returnsFilteredList() {
    mockUser();
    ExpenseEntity entity = buildExpenseEntity();
    ExpenseResponse response = buildExpenseResponse(1L);

    when(expenseRepository.findAllByUser_IdAndStatusType(USER_ID, StatusType.PAID))
        .thenReturn(List.of(entity));
    when(expenseMapper.toResponse(entity)).thenReturn(response);

    List<ExpenseResponse> result = expenseService.findAllByStatus(EMAIL, StatusType.PAID);

    assertThat(result).hasSize(1);
  }

  @Test
  void findAllByCategory_validCategory_returnsFilteredList() {
    mockUser();
    CategoryEntity category = buildCategoryEntity(1L);
    ExpenseEntity entity = buildExpenseEntity();
    ExpenseResponse response = buildExpenseResponse(1L);

    when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
    when(expenseRepository.findAllByUser_IdAndCategory_Id(USER_ID, 1L))
        .thenReturn(List.of(entity));
    when(expenseMapper.toResponse(entity)).thenReturn(response);

    List<ExpenseResponse> result = expenseService.findAllByCategory(EMAIL, 1L);

    assertThat(result).hasSize(1);
  }

  @Test
  void findAllByCategory_categoryNotFound_throwsCategoryNotFoundException() {
    mockUser();
    when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> expenseService.findAllByCategory(EMAIL, 99L))
        .isInstanceOf(CategoryNotFoundException.class);
  }

  private UserEntity buildUserEntity() {
    UserEntity user = new UserEntity();
    ReflectionTestUtils.setField(user, "id", USER_ID);
    user.setEmail(EMAIL);
    return user;
  }

  private ExpenseEntity buildExpenseEntity() {
    ExpenseEntity e = new ExpenseEntity();
    e.setAmount(BigDecimal.TEN);
    e.setDescription("Lunch");
    e.setDate(LocalDate.now());
    e.setStatusType(StatusType.PAID);
    e.setCurrencyType(CurrencyType.USD);
    e.setCategory(buildCategoryEntity(1L));
    return e;
  }

  private CategoryEntity buildCategoryEntity(Long id) {
    CategoryEntity c = new CategoryEntity();
    c.setName("Food");
    c.setIcon("🍔");
    c.setColor("red");
    c.setCategoryType(CategoryType.EXPENSE);
    return c;
  }

  private ExpenseResponse buildExpenseResponse(Long id) {
    return new ExpenseResponse(
        id,
        BigDecimal.TEN,
        "Lunch",
        null,
        LocalDate.now(),
        null,
        StatusType.PAID,
        CurrencyType.USD,
        LocalDateTime.now(),
        LocalDateTime.now());
  }
}

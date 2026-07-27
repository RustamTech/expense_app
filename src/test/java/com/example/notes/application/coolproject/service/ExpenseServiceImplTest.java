package com.example.notes.application.coolproject.service;

import com.example.notes.application.coolproject.dto.expenseDto.ExpenseCreateRequest;
import com.example.notes.application.coolproject.dto.expenseDto.ExpenseResponse;
import com.example.notes.application.coolproject.dto.expenseDto.ExpenseUpdateRequest;
import com.example.notes.application.coolproject.entity.CategoryEntity;
import com.example.notes.application.coolproject.entity.ExpenseEntity;
import com.example.notes.application.coolproject.enums.CategoryType;
import com.example.notes.application.coolproject.enums.CurrencyType;
import com.example.notes.application.coolproject.enums.StatusType;
import com.example.notes.application.coolproject.exception.custom.CategoryNotFoundException;
import com.example.notes.application.coolproject.exception.custom.ExpenseNotFoundException;
import com.example.notes.application.coolproject.mapper.ExpenseMapper;
import com.example.notes.application.coolproject.repository.CategoryRepository;
import com.example.notes.application.coolproject.repository.ExpenseRepository;
import com.example.notes.application.coolproject.service.expense.ExpenseServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceImplTest {

    @Mock
    private ExpenseMapper expenseMapper;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    @Test
    void findById_exists_returnsResponse() {
        ExpenseEntity entity = buildExpenseEntity();
        ExpenseResponse response = buildExpenseResponse(1L);

        when(expenseRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(expenseMapper.toResponse(entity)).thenReturn(response);

        ExpenseResponse result = expenseService.findById(1L);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void findById_notFound_throwsExpenseNotFoundException() {
        when(expenseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.findById(99L))
                .isInstanceOf(ExpenseNotFoundException.class);
    }

    @Test
    void create_validRequest_returnsSavedResponse() {
        ExpenseCreateRequest request = new ExpenseCreateRequest(
                BigDecimal.TEN, "Lunch", null, CurrencyType.USD, LocalDate.now(), 1L, StatusType.PAID);
        CategoryEntity category = buildCategoryEntity(1L);
        ExpenseEntity entity = buildExpenseEntity();
        ExpenseResponse response = buildExpenseResponse(1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(expenseMapper.toEntity(request)).thenReturn(entity);
        when(expenseRepository.save(entity)).thenReturn(entity);
        when(expenseMapper.toResponse(entity)).thenReturn(response);

        ExpenseResponse result = expenseService.create(request);

        assertThat(result).isEqualTo(response);
        verify(expenseRepository).save(entity);
    }

    @Test
    void create_categoryNotFound_throwsCategoryNotFoundException() {
        ExpenseCreateRequest request = new ExpenseCreateRequest(
                BigDecimal.TEN, "Lunch", null, CurrencyType.USD, LocalDate.now(), 99L, StatusType.PAID);

        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.create(request))
                .isInstanceOf(CategoryNotFoundException.class);

        verify(expenseRepository, never()).save(any());
    }

    @Test
    void update_existingExpense_returnsUpdatedResponse() {
        ExpenseUpdateRequest request = new ExpenseUpdateRequest(
                BigDecimal.valueOf(20), "Dinner", null, CurrencyType.EUR, LocalDate.now(), null, StatusType.PLANNED);
        ExpenseEntity entity = buildExpenseEntity();
        ExpenseResponse response = buildExpenseResponse(1L);

        when(expenseRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(expenseRepository.save(entity)).thenReturn(entity);
        when(expenseMapper.toResponse(entity)).thenReturn(response);

        ExpenseResponse result = expenseService.update(1L, request);

        assertThat(result).isEqualTo(response);
        verify(expenseMapper).updateEntity(request, entity);
    }

    @Test
    void update_notFound_throwsExpenseNotFoundException() {
        ExpenseUpdateRequest request = new ExpenseUpdateRequest(
                BigDecimal.TEN, "Lunch", null, CurrencyType.USD, LocalDate.now(), null, StatusType.PAID);

        when(expenseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.update(99L, request))
                .isInstanceOf(ExpenseNotFoundException.class);
    }

    @Test
    void update_withNewCategory_loadsAndAssignsCategory() {
        ExpenseUpdateRequest request = new ExpenseUpdateRequest(
                BigDecimal.TEN, "Lunch", null, CurrencyType.USD, LocalDate.now(), 2L, StatusType.PAID);
        ExpenseEntity entity = buildExpenseEntity();
        CategoryEntity newCategory = buildCategoryEntity(2L);
        ExpenseResponse response = buildExpenseResponse(1L);

        when(expenseRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));
        when(expenseRepository.save(entity)).thenReturn(entity);
        when(expenseMapper.toResponse(entity)).thenReturn(response);

        expenseService.update(1L, request);

        assertThat(entity.getCategory()).isEqualTo(newCategory);
    }

    @Test
    void update_newCategoryNotFound_throwsCategoryNotFoundException() {
        ExpenseUpdateRequest request = new ExpenseUpdateRequest(
                BigDecimal.TEN, "Lunch", null, CurrencyType.USD, LocalDate.now(), 99L, StatusType.PAID);
        ExpenseEntity entity = buildExpenseEntity();

        when(expenseRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.update(1L, request))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void delete_existingExpense_deletesSuccessfully() {
        ExpenseEntity entity = buildExpenseEntity();
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(entity));

        expenseService.delete(1L);

        verify(expenseRepository).delete(entity);
    }

    @Test
    void delete_notFound_throwsExpenseNotFoundException() {
        when(expenseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.delete(99L))
                .isInstanceOf(ExpenseNotFoundException.class);

        verify(expenseRepository, never()).delete(any());
    }

    @Test
    void findAll_returnsMappedList() {
        ExpenseEntity entity = buildExpenseEntity();
        ExpenseResponse response = buildExpenseResponse(1L);

        when(expenseRepository.findAll()).thenReturn(List.of(entity));
        when(expenseMapper.toResponse(entity)).thenReturn(response);

        List<ExpenseResponse> result = expenseService.findAll();

        assertThat(result).hasSize(1).containsExactly(response);
    }

    @Test
    void findAllByStatus_returnsFilteredList() {
        ExpenseEntity entity = buildExpenseEntity();
        ExpenseResponse response = buildExpenseResponse(1L);

        when(expenseRepository.findAllByStatusType(StatusType.PAID)).thenReturn(List.of(entity));
        when(expenseMapper.toResponse(entity)).thenReturn(response);

        List<ExpenseResponse> result = expenseService.findAllByStatus(StatusType.PAID);

        assertThat(result).hasSize(1);
    }

    @Test
    void findAllByCategory_validCategory_returnsFilteredList() {
        CategoryEntity category = buildCategoryEntity(1L);
        ExpenseEntity entity = buildExpenseEntity();
        ExpenseResponse response = buildExpenseResponse(1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(expenseRepository.findAllByCategory_Id(1L)).thenReturn(List.of(entity));
        when(expenseMapper.toResponse(entity)).thenReturn(response);

        List<ExpenseResponse> result = expenseService.findAllByCategory(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void findAllByCategory_categoryNotFound_throwsCategoryNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.findAllByCategory(99L))
                .isInstanceOf(CategoryNotFoundException.class);
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
        return new ExpenseResponse(id, BigDecimal.TEN, "Lunch", null, LocalDate.now(),
                null, StatusType.PAID, CurrencyType.USD, LocalDateTime.now(), LocalDateTime.now());
    }
}

package com.example.notes.application.coolproject.controller;

import static com.example.notes.application.coolproject.TestData.USER_EMAIL;
import static com.example.notes.application.coolproject.TestData.expenseResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.notes.application.coolproject.TestData;
import com.example.notes.application.coolproject.dto.expense.ExpenseCreateRequest;
import com.example.notes.application.coolproject.dto.expense.ExpenseUpdateRequest;
import com.example.notes.application.coolproject.enums.CurrencyType;
import com.example.notes.application.coolproject.enums.StatusType;
import com.example.notes.application.coolproject.exception.custom.CategoryNotFoundException;
import com.example.notes.application.coolproject.exception.custom.ExpenseNotFoundException;
import com.example.notes.application.coolproject.service.expense.ExpenseService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(ExpenseController.class)
@WithMockUser(username = USER_EMAIL)
class ExpenseControllerTest extends AbstractControllerTest {

  private static final String EXPENSES = "/api/notes/expenses";

  @MockitoBean private ExpenseService expenseService;

  @Test
  void listsOwnExpensesOfCaller() throws Exception {
    when(expenseService.findAll(USER_EMAIL))
        .thenReturn(List.of(expenseResponse(1L), expenseResponse(2L)));

    mockMvc
        .perform(get(EXPENSES))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));
  }

  @Test
  void returnsOwnExpense() throws Exception {
    when(expenseService.findById(USER_EMAIL, 1L)).thenReturn(expenseResponse(1L));

    mockMvc
        .perform(get(EXPENSES + "/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.description").value("Lunch"))
        .andExpect(jsonPath("$.category.name").value("Food"));
  }

  @Test
  void foreignOrMissingExpenseReturns404() throws Exception {
    when(expenseService.findById(USER_EMAIL, 99L)).thenThrow(new ExpenseNotFoundException(99L));

    mockMvc
        .perform(get(EXPENSES + "/99"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("EXPENSE_NOT_FOUND"));
  }

  @Test
  void filtersByStatus() throws Exception {
    when(expenseService.findAllByStatus(USER_EMAIL, StatusType.PAID))
        .thenReturn(List.of(expenseResponse(1L)));

    mockMvc
        .perform(get(EXPENSES + "/status/PAID"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void filtersByCategory() throws Exception {
    when(expenseService.findAllByCategory(USER_EMAIL, 1L)).thenReturn(List.of(expenseResponse(1L)));

    mockMvc.perform(get(EXPENSES + "/category/1")).andExpect(status().isOk());
  }

  @Test
  void filterByMissingCategoryReturns404() throws Exception {
    when(expenseService.findAllByCategory(USER_EMAIL, 9L))
        .thenThrow(new CategoryNotFoundException(9L));

    mockMvc
        .perform(get(EXPENSES + "/category/9"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"));
  }

  @Test
  void createsExpenseForCaller() throws Exception {
    when(expenseService.create(eq(USER_EMAIL), any())).thenReturn(expenseResponse(1L));

    mockMvc
        .perform(post(EXPENSES).contentType(JSON).content(json(validCreate())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1));
  }

  @Test
  void amountWithTooManyDecimalsReturns400() throws Exception {
    var request =
        new ExpenseCreateRequest(
            new BigDecimal("1.999"),
            "Lunch",
            null,
            CurrencyType.AZN,
            TestData.DAY,
            1L,
            StatusType.PAID);

    mockMvc
        .perform(post(EXPENSES).contentType(JSON).content(json(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].field").value("amount"));
  }

  @Test
  void updatesOwnExpense() throws Exception {
    var request =
        new ExpenseUpdateRequest(new BigDecimal("20"), null, null, null, null, null, null);
    when(expenseService.update(eq(USER_EMAIL), eq(1L), any())).thenReturn(expenseResponse(1L));

    mockMvc
        .perform(put(EXPENSES + "/1").contentType(JSON).content(json(request)))
        .andExpect(status().isOk());
  }

  @Test
  void updateWithBlankDescriptionReturns400() throws Exception {
    var request = new ExpenseUpdateRequest(null, " ", null, null, null, null, null);

    mockMvc
        .perform(put(EXPENSES + "/1").contentType(JSON).content(json(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].field").value("description"));
  }

  @Test
  void deletesOwnExpense() throws Exception {
    mockMvc.perform(delete(EXPENSES + "/1")).andExpect(status().isNoContent());

    verify(expenseService).delete(USER_EMAIL, 1L);
  }

  @Test
  void deletingForeignExpenseReturns404() throws Exception {
    doThrow(new ExpenseNotFoundException(5L)).when(expenseService).delete(USER_EMAIL, 5L);

    mockMvc.perform(delete(EXPENSES + "/5")).andExpect(status().isNotFound());
  }

  private static ExpenseCreateRequest validCreate() {
    return new ExpenseCreateRequest(
        new BigDecimal("12.50"),
        "Lunch",
        null,
        CurrencyType.AZN,
        TestData.DAY,
        1L,
        StatusType.PAID);
  }
}

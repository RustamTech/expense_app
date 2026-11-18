package com.example.notes.application.coolproject.controller;

import com.example.notes.application.coolproject.config.Endpoints;
import com.example.notes.application.coolproject.dto.expense.ExpenseCreateRequest;
import com.example.notes.application.coolproject.dto.expense.ExpenseResponse;
import com.example.notes.application.coolproject.dto.expense.ExpenseUpdateRequest;
import com.example.notes.application.coolproject.enums.StatusType;
import com.example.notes.application.coolproject.service.expense.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Expenses of the authenticated user. Foreign expenses answer 404, exactly like missing ones. */
@Tag(name = "Expenses", description = "Own expenses of the authenticated user")
@ApiResponse(responseCode = "401", description = "Missing or invalid token")
@RestController
@RequestMapping(Endpoints.EXPENSES)
@RequiredArgsConstructor
public class ExpenseController {

  private final ExpenseService expenseService;

  /**
   * Returns one own expense.
   *
   * @param authentication the caller
   * @param id expense identifier
   * @return the expense
   */
  @Operation(summary = "Get an own expense by id")
  @ApiResponse(responseCode = "200", description = "Expense found")
  @ApiResponse(responseCode = "404", description = "Expense not found")
  @GetMapping(Endpoints.EXPENSE_BY_ID)
  public ResponseEntity<ExpenseResponse> findById(
      Authentication authentication, @PathVariable Long id) {
    return ResponseEntity.ok(expenseService.findById(authentication.getName(), id));
  }

  /**
   * Returns all own expenses, newest first.
   *
   * @param authentication the caller
   * @return the expenses
   */
  @Operation(summary = "Get all own expenses")
  @ApiResponse(responseCode = "200", description = "Expenses")
  @GetMapping
  public ResponseEntity<List<ExpenseResponse>> findAll(Authentication authentication) {
    return ResponseEntity.ok(expenseService.findAll(authentication.getName()));
  }

  /**
   * Returns own expenses with the given status.
   *
   * @param authentication the caller
   * @param status {@code PENDING}, {@code PLANNED} or {@code PAID}
   * @return the matching expenses
   */
  @Operation(summary = "Get own expenses by status")
  @ApiResponse(responseCode = "200", description = "Expenses")
  @ApiResponse(responseCode = "400", description = "Unknown status")
  @GetMapping(Endpoints.EXPENSES_BY_STATUS)
  public ResponseEntity<List<ExpenseResponse>> findAllByStatus(
      Authentication authentication, @PathVariable StatusType status) {
    return ResponseEntity.ok(expenseService.findAllByStatus(authentication.getName(), status));
  }

  /**
   * Returns own expenses in the given category.
   *
   * @param authentication the caller
   * @param categoryId category identifier
   * @return the matching expenses
   */
  @Operation(summary = "Get own expenses by category")
  @ApiResponse(responseCode = "200", description = "Expenses")
  @ApiResponse(responseCode = "404", description = "Category not found")
  @GetMapping(Endpoints.EXPENSES_BY_CATEGORY)
  public ResponseEntity<List<ExpenseResponse>> findAllByCategory(
      Authentication authentication, @PathVariable Long categoryId) {
    return ResponseEntity.ok(
        expenseService.findAllByCategory(authentication.getName(), categoryId));
  }

  /**
   * Creates an own expense.
   *
   * @param authentication the caller, who becomes the owner
   * @param request create payload
   * @return 201 with the created expense
   */
  @Operation(summary = "Create an expense")
  @ApiResponse(responseCode = "201", description = "Expense created")
  @ApiResponse(responseCode = "400", description = "Invalid request body")
  @ApiResponse(responseCode = "404", description = "Category not found")
  @PostMapping
  public ResponseEntity<ExpenseResponse> create(
      Authentication authentication, @Valid @RequestBody ExpenseCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(expenseService.create(authentication.getName(), request));
  }

  /**
   * Changes an own expense.
   *
   * @param authentication the caller
   * @param id expense identifier
   * @param request partial update payload
   * @return the updated expense
   */
  @Operation(summary = "Update an own expense")
  @ApiResponse(responseCode = "200", description = "Expense updated")
  @ApiResponse(responseCode = "400", description = "Invalid request body")
  @ApiResponse(responseCode = "404", description = "Expense or category not found")
  @PutMapping(Endpoints.EXPENSE_BY_ID)
  public ResponseEntity<ExpenseResponse> update(
      Authentication authentication,
      @PathVariable Long id,
      @Valid @RequestBody ExpenseUpdateRequest request) {
    return ResponseEntity.ok(expenseService.update(authentication.getName(), id, request));
  }

  /**
   * Deletes an own expense.
   *
   * @param authentication the caller
   * @param id expense identifier
   * @return empty 204 response
   */
  @Operation(summary = "Delete an own expense")
  @ApiResponse(responseCode = "204", description = "Expense deleted")
  @ApiResponse(responseCode = "404", description = "Expense not found")
  @DeleteMapping(Endpoints.EXPENSE_BY_ID)
  public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
    expenseService.delete(authentication.getName(), id);
    return ResponseEntity.noContent().build();
  }
}

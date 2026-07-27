package com.example.notes.application.coolproject.controller;

import com.example.notes.application.coolproject.config.Endpoints;
import com.example.notes.application.coolproject.dto.expenseDto.ExpenseCreateRequest;
import com.example.notes.application.coolproject.dto.expenseDto.ExpenseResponse;
import com.example.notes.application.coolproject.dto.expenseDto.ExpenseUpdateRequest;
import com.example.notes.application.coolproject.enums.StatusType;
import com.example.notes.application.coolproject.service.expense.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Expenses", description = "Expense management endpoints")
@RestController
@RequestMapping(Endpoints.EXPENSES)
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @Operation(summary = "Get expense by id")
    @GetMapping(Endpoints.EXPENSE_BY_ID)
    public ResponseEntity<ExpenseResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.findById(id));
    }

    @Operation(summary = "Get all expenses")
    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> findAll() {
        return ResponseEntity.ok(expenseService.findAll());
    }

    @Operation(summary = "Get all expenses by status")
    @GetMapping(Endpoints.EXPENSES_BY_STATUS)
    public ResponseEntity<List<ExpenseResponse>> findAllByStatus(@PathVariable StatusType status) {
        return ResponseEntity.ok(expenseService.findAllByStatus(status));
    }

    @Operation(summary = "Create new expense")
    @PostMapping
    public ResponseEntity<ExpenseResponse> create(@Valid @RequestBody ExpenseCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.create(request));
    }

    @Operation(summary = "Update expense")
    @PutMapping(Endpoints.EXPENSE_BY_ID)
    public ResponseEntity<ExpenseResponse> update(@PathVariable Long id, @Valid @RequestBody ExpenseUpdateRequest request) {
        return ResponseEntity.ok(expenseService.update(id, request));
    }

    @Operation(summary = "Delete expense")
    @DeleteMapping(Endpoints.EXPENSE_BY_ID)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        expenseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all expenses by category")
    @GetMapping(Endpoints.EXPENSES_BY_CATEGORY)
    public ResponseEntity<List<ExpenseResponse>> findAllByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(expenseService.findAllByCategory(categoryId));
    }
}

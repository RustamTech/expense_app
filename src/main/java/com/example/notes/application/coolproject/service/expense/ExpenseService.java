package com.example.notes.application.coolproject.service.expense;

import com.example.notes.application.coolproject.dto.expenseDto.ExpenseCreateRequest;
import com.example.notes.application.coolproject.dto.expenseDto.ExpenseResponse;
import com.example.notes.application.coolproject.dto.expenseDto.ExpenseUpdateRequest;
import com.example.notes.application.coolproject.enums.StatusType;

import java.util.List;

public interface ExpenseService {

    ExpenseResponse findById(Long id);

    List<ExpenseResponse> findAll();

    List<ExpenseResponse> findAllByStatus(StatusType statusType);

    List<ExpenseResponse> findAllByCategory(Long categoryId);

    void delete(Long id);

    ExpenseResponse update(Long id, ExpenseUpdateRequest request);

    ExpenseResponse create(ExpenseCreateRequest request);
}

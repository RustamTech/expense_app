package com.example.notes.application.coolproject.service.expense;

import com.example.notes.application.coolproject.dto.expenseDto.ExpenseCreateRequest;
import com.example.notes.application.coolproject.dto.expenseDto.ExpenseResponse;
import com.example.notes.application.coolproject.dto.expenseDto.ExpenseUpdateRequest;
import com.example.notes.application.coolproject.enums.StatusType;
import java.util.List;

public interface ExpenseService {

  ExpenseResponse findById(String email, Long id);

  List<ExpenseResponse> findAll(String email);

  List<ExpenseResponse> findAllByStatus(String email, StatusType statusType);

  List<ExpenseResponse> findAllByCategory(String email, Long categoryId);

  void delete(String email, Long id);

  ExpenseResponse update(String email, Long id, ExpenseUpdateRequest request);

  ExpenseResponse create(String email, ExpenseCreateRequest request);
}

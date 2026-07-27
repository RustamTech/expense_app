package com.example.notes.application.coolproject.service.expense;

import com.example.notes.application.coolproject.dto.expenseDto.ExpenseCreateRequest;
import com.example.notes.application.coolproject.dto.expenseDto.ExpenseResponse;
import com.example.notes.application.coolproject.dto.expenseDto.ExpenseUpdateRequest;
import com.example.notes.application.coolproject.entity.CategoryEntity;
import com.example.notes.application.coolproject.entity.ExpenseEntity;
import com.example.notes.application.coolproject.enums.StatusType;
import com.example.notes.application.coolproject.exception.custom.CategoryNotFoundException;
import com.example.notes.application.coolproject.exception.custom.ExpenseNotFoundException;
import com.example.notes.application.coolproject.mapper.ExpenseMapper;
import com.example.notes.application.coolproject.repository.CategoryRepository;
import com.example.notes.application.coolproject.repository.ExpenseRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseMapper expenseMapper;
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public ExpenseResponse findById(Long id) {
        log.info("Fetching expense by id: {}", id);
        ExpenseEntity expense = expenseRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Expense not found with this id: {}", id);
                    return new ExpenseNotFoundException(id);
                });
        log.info("Successfully fetched expense with id: {}", id);
        return expenseMapper.toResponse(expense);
    }

    @Override
    @Transactional
    public void delete(Long id){
        log.info("Delete expense by id: {}", id);
        ExpenseEntity expense = expenseRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Expense already deleted by id: {}", id);
                    return new ExpenseNotFoundException(id);
                });
        expenseRepository.delete(expense);
        log.info("Successfully deleted expense by id: {}", id);
    }

    @Override
    @Transactional
    public ExpenseResponse create(ExpenseCreateRequest request){
        log.info("Creating expense with description: {}", request.description());
        CategoryEntity category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));
        ExpenseEntity expense = expenseMapper.toEntity(request);
        expense.setCategory(category);
        ExpenseEntity saved = expenseRepository.save(expense);
        log.info("Successfully created expense with id: {}", saved.getId());
        return expenseMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ExpenseResponse update(Long id, ExpenseUpdateRequest request) {
        log.info("Updating expense with id: {}", id);
        ExpenseEntity expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ExpenseNotFoundException(id));
        if (request.categoryId() != null) {
            CategoryEntity category = categoryRepository.findById(request.categoryId())
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
    public List<ExpenseResponse> findAll(){
        log.info("Fetching all expenses");
        List<ExpenseResponse> expenses = expenseRepository.findAll()
                .stream()
                .map(expenseMapper::toResponse)
                .toList();
        log.info("Successfully fetched all expenses");
        return expenses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponse> findAllByStatus(StatusType statusType){
        log.info("Fetching all expenses by status: {}", statusType);
        List<ExpenseResponse> expenses = expenseRepository.findAllByStatusType(statusType)
                .stream()
                .map(expenseMapper::toResponse)
                .toList();
        log.info("Successfully fetched all expenses by status: {}", statusType);
        return expenses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponse> findAllByCategory(Long categoryId){
        log.info("Fetching all expenses by category: {}", categoryId);
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
        return expenseRepository.findAllByCategory_Id(categoryId)
                .stream()
                .map(expenseMapper::toResponse)
                .toList();
    }
}

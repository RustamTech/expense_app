package com.example.notes.application.coolproject.dto.expenseDto;

import com.example.notes.application.coolproject.dto.categoryDto.CategoryResponse;
import com.example.notes.application.coolproject.enums.CurrencyType;
import com.example.notes.application.coolproject.enums.StatusType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExpenseResponse(
        Long id,
        BigDecimal amount,
        String description,
        String note,
        LocalDate date,
        CategoryResponse category,
        StatusType statusType,
        CurrencyType currencyType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

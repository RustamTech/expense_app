package com.example.notes.application.coolproject.dto.expense;

import com.example.notes.application.coolproject.dto.category.CategoryResponse;
import com.example.notes.application.coolproject.enums.CurrencyType;
import com.example.notes.application.coolproject.enums.StatusType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * An expense of the authenticated user.
 *
 * @param id identifier
 * @param amount amount
 * @param description short description
 * @param note optional note
 * @param date day of the expense
 * @param category catalog category
 * @param statusType payment status
 * @param currencyType currency
 * @param createdAt creation time
 * @param updatedAt last modification time
 */
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
    LocalDateTime updatedAt) {}

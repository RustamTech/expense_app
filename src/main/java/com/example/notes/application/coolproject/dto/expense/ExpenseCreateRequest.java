package com.example.notes.application.coolproject.dto.expense;

import com.example.notes.application.coolproject.enums.CurrencyType;
import com.example.notes.application.coolproject.enums.StatusType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payload for creating an expense owned by the authenticated user.
 *
 * @param amount positive amount with at most 2 decimal places
 * @param description short description
 * @param note optional longer note
 * @param currencyType currency of the amount
 * @param date day the expense happened
 * @param categoryId catalog category
 * @param statusType payment status
 */
public record ExpenseCreateRequest(
    @Schema(description = "Amount", example = "12.50")
        @NotNull @Positive @Digits(
            integer = ExpenseLimits.AMOUNT_INTEGER_DIGITS,
            fraction = ExpenseLimits.AMOUNT_FRACTION_DIGITS)
        BigDecimal amount,
    @Schema(description = "Short description", example = "Lunch")
        @NotBlank @Size(max = ExpenseLimits.DESCRIPTION_MAX) String description,
    @Schema(description = "Optional note", example = "With my sister")
        @Size(max = ExpenseLimits.NOTE_MAX) String note,
    @Schema(description = "Currency", example = "AZN") @NotNull CurrencyType currencyType,
    @Schema(description = "Day of the expense", example = "2026-01-14") @NotNull LocalDate date,
    @Schema(description = "Category id", example = "1") @NotNull @Positive Long categoryId,
    @Schema(description = "Payment status", example = "PAID") @NotNull StatusType statusType) {}

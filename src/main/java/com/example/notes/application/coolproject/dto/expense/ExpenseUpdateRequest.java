package com.example.notes.application.coolproject.dto.expense;

import com.example.notes.application.coolproject.dto.NotBlankIfPresent;
import com.example.notes.application.coolproject.enums.CurrencyType;
import com.example.notes.application.coolproject.enums.StatusType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Partial update of an own expense. {@code null} fields are left unchanged.
 *
 * @param amount new positive amount
 * @param description new description, not blank if present
 * @param note new note
 * @param currencyType new currency
 * @param date new day
 * @param categoryId new catalog category
 * @param statusType new payment status
 */
public record ExpenseUpdateRequest(
    @Schema(description = "Amount", example = "12.50")
        @Positive @Digits(
            integer = ExpenseLimits.AMOUNT_INTEGER_DIGITS,
            fraction = ExpenseLimits.AMOUNT_FRACTION_DIGITS)
        BigDecimal amount,
    @Schema(description = "Short description", example = "Lunch")
        @Size(max = ExpenseLimits.DESCRIPTION_MAX) @Pattern(regexp = NotBlankIfPresent.PATTERN, message = NotBlankIfPresent.MESSAGE) String description,
    @Schema(description = "Optional note", example = "With my sister")
        @Size(max = ExpenseLimits.NOTE_MAX) String note,
    @Schema(description = "Currency", example = "AZN") CurrencyType currencyType,
    @Schema(description = "Day of the expense", example = "2026-01-14") LocalDate date,
    @Schema(description = "Category id", example = "1") @Positive Long categoryId,
    @Schema(description = "Payment status", example = "PAID") StatusType statusType) {}

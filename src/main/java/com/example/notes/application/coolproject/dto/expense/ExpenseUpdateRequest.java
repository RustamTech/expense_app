package com.example.notes.application.coolproject.dto.expense;

import com.example.notes.application.coolproject.enums.CurrencyType;
import com.example.notes.application.coolproject.enums.StatusType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseUpdateRequest(
    @Schema(description = "Number of amount", example = "12") @Positive BigDecimal amount,
    @Schema(description = "Description of expense", example = "Spent money for food")
        String description,
    @Schema(
            description = "Additional information to expense",
            example = "Spent money with my sister")
        String note,
    @Schema(description = "Type of currency", example = "UZB") CurrencyType currencyType,
    @Schema(description = "Time when expense created", example = "2004-01-14") LocalDate date,
    @Schema(description = "Unique id of category", example = "1") Long categoryId,
    @Schema(description = "Type of status", example = "PAID") StatusType statusType) {}

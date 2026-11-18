package com.example.notes.application.coolproject.dto.statistics;

import com.example.notes.application.coolproject.enums.CurrencyType;
import com.example.notes.application.coolproject.enums.StatusType;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Spending summary of the authenticated user. Amounts are never summed across currencies, so every
 * monetary figure is grouped by currency first.
 *
 * @param totalCount number of expenses
 * @param totalByCurrency total amount per currency
 * @param amountByCategory per currency, the total amount per category name
 * @param countByStatus number of expenses per payment status
 */
public record StatisticsSummaryResponse(
    long totalCount,
    Map<CurrencyType, BigDecimal> totalByCurrency,
    Map<CurrencyType, Map<String, BigDecimal>> amountByCategory,
    Map<StatusType, Long> countByStatus) {}

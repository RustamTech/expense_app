package com.example.notes.application.coolproject.dto.statistics;

import com.example.notes.application.coolproject.enums.CurrencyType;
import com.example.notes.application.coolproject.enums.StatusType;
import java.math.BigDecimal;
import java.util.Map;

public record StatisticsSummaryResponse(
    BigDecimal totalAmount,
    long totalCount,
    Map<String, BigDecimal> amountByCategory,
    Map<StatusType, Long> countByStatus,
    Map<CurrencyType, BigDecimal> amountByCurrency) {}

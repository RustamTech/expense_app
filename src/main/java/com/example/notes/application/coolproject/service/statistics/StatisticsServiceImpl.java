package com.example.notes.application.coolproject.service.statistics;

import com.example.notes.application.coolproject.dto.statisticsDto.StatisticsSummaryResponse;
import com.example.notes.application.coolproject.entity.ExpenseEntity;
import com.example.notes.application.coolproject.enums.CurrencyType;
import com.example.notes.application.coolproject.enums.StatusType;
import com.example.notes.application.coolproject.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final ExpenseRepository expenseRepository;

    @Override
    @Transactional(readOnly = true)
    public StatisticsSummaryResponse getSummary() {
        log.info("Generating statistics summary");
        List<ExpenseEntity> all = expenseRepository.findAll();

        BigDecimal total = all.stream()
                .map(ExpenseEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> byCategory = all.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCategory().getName(),
                        Collectors.reducing(BigDecimal.ZERO, ExpenseEntity::getAmount, BigDecimal::add)
                ));

        Map<StatusType, Long> byStatus = all.stream()
                .filter(e -> e.getStatusType() != null)
                .collect(Collectors.groupingBy(ExpenseEntity::getStatusType, Collectors.counting()));

        Map<CurrencyType, BigDecimal> byCurrency = all.stream()
                .filter(e -> e.getCurrencyType() != null)
                .collect(Collectors.groupingBy(
                        ExpenseEntity::getCurrencyType,
                        Collectors.reducing(BigDecimal.ZERO, ExpenseEntity::getAmount, BigDecimal::add)
                ));

        log.info("Statistics summary generated: {} expenses, total {}", all.size(), total);
        return new StatisticsSummaryResponse(total, all.size(), byCategory, byStatus, byCurrency);
    }
}

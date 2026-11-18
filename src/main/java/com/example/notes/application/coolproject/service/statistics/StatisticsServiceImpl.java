package com.example.notes.application.coolproject.service.statistics;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;

import com.example.notes.application.coolproject.dto.statistics.StatisticsSummaryResponse;
import com.example.notes.application.coolproject.entity.ExpenseEntity;
import com.example.notes.application.coolproject.enums.CurrencyType;
import com.example.notes.application.coolproject.enums.StatusType;
import com.example.notes.application.coolproject.exception.custom.UserNotFoundException;
import com.example.notes.application.coolproject.repository.ExpenseRepository;
import com.example.notes.application.coolproject.repository.UserRepository;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@inheritDoc} Aggregation happens in memory over the owner's expenses; expenses without a
 * currency are left out of the monetary figures but still counted.
 */
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

  private final ExpenseRepository expenseRepository;
  private final UserRepository userRepository;

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public StatisticsSummaryResponse getSummary(String email) {
    Long userId =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> UserNotFoundException.byEmail(email))
            .getId();
    List<ExpenseEntity> expenses = expenseRepository.findAllByUser_IdOrderByDateDesc(userId);
    List<ExpenseEntity> withCurrency =
        expenses.stream().filter(e -> e.getCurrencyType() != null).toList();

    Map<CurrencyType, BigDecimal> totalByCurrency =
        withCurrency.stream()
            .collect(
                groupingBy(
                    ExpenseEntity::getCurrencyType,
                    () -> new EnumMap<>(CurrencyType.class),
                    sumOfAmounts()));

    Map<CurrencyType, Map<String, BigDecimal>> amountByCategory =
        withCurrency.stream()
            .collect(
                groupingBy(
                    ExpenseEntity::getCurrencyType,
                    () -> new EnumMap<>(CurrencyType.class),
                    groupingBy(e -> e.getCategory().getName(), TreeMap::new, sumOfAmounts())));

    Map<StatusType, Long> countByStatus =
        expenses.stream()
            .map(ExpenseEntity::getStatusType)
            .filter(Objects::nonNull)
            .collect(groupingBy(s -> s, () -> new EnumMap<>(StatusType.class), counting()));

    return new StatisticsSummaryResponse(
        expenses.size(), totalByCurrency, amountByCategory, countByStatus);
  }

  private static Collector<ExpenseEntity, ?, BigDecimal> sumOfAmounts() {
    return reducing(BigDecimal.ZERO, ExpenseEntity::getAmount, BigDecimal::add);
  }
}

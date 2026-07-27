package com.example.notes.application.coolproject.service.statistics;

import com.example.notes.application.coolproject.dto.statisticsDto.StatisticsSummaryResponse;
import com.example.notes.application.coolproject.entity.ExpenseEntity;
import com.example.notes.application.coolproject.entity.UserEntity;
import com.example.notes.application.coolproject.enums.CurrencyType;
import com.example.notes.application.coolproject.enums.StatusType;
import com.example.notes.application.coolproject.exception.custom.UserNotFoundException;
import com.example.notes.application.coolproject.repository.ExpenseRepository;
import com.example.notes.application.coolproject.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

  private final ExpenseRepository expenseRepository;
  private final UserRepository userRepository;

  @Override
  @Transactional(readOnly = true)
  public StatisticsSummaryResponse getSummary(String email) {
    log.info("Generating statistics summary");
    UserEntity user =
        userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
    List<ExpenseEntity> all = expenseRepository.findAllByUser_Id(user.getId());

    BigDecimal total =
        all.stream().map(ExpenseEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

    Map<String, BigDecimal> byCategory =
        all.stream()
            .collect(
                Collectors.groupingBy(
                    e -> e.getCategory().getName(),
                    Collectors.reducing(
                        BigDecimal.ZERO, ExpenseEntity::getAmount, BigDecimal::add)));

    Map<StatusType, Long> byStatus =
        all.stream()
            .filter(e -> e.getStatusType() != null)
            .collect(Collectors.groupingBy(ExpenseEntity::getStatusType, Collectors.counting()));

    Map<CurrencyType, BigDecimal> byCurrency =
        all.stream()
            .filter(e -> e.getCurrencyType() != null)
            .collect(
                Collectors.groupingBy(
                    ExpenseEntity::getCurrencyType,
                    Collectors.reducing(
                        BigDecimal.ZERO, ExpenseEntity::getAmount, BigDecimal::add)));

    log.info("Statistics summary generated: {} expenses, total {}", all.size(), total);
    return new StatisticsSummaryResponse(total, all.size(), byCategory, byStatus, byCurrency);
  }
}

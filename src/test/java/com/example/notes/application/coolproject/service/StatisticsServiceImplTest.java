package com.example.notes.application.coolproject.service;

import static com.example.notes.application.coolproject.TestData.USER_EMAIL;
import static com.example.notes.application.coolproject.TestData.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.notes.application.coolproject.TestData;
import com.example.notes.application.coolproject.entity.CategoryEntity;
import com.example.notes.application.coolproject.entity.ExpenseEntity;
import com.example.notes.application.coolproject.enums.CurrencyType;
import com.example.notes.application.coolproject.enums.StatusType;
import com.example.notes.application.coolproject.exception.custom.UserNotFoundException;
import com.example.notes.application.coolproject.repository.ExpenseRepository;
import com.example.notes.application.coolproject.repository.UserRepository;
import com.example.notes.application.coolproject.service.statistics.StatisticsServiceImpl;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceImplTest {

  @Mock private ExpenseRepository expenseRepository;
  @Mock private UserRepository userRepository;

  @InjectMocks private StatisticsServiceImpl statisticsService;

  private final CategoryEntity food = TestData.category(1L, "Food");
  private final CategoryEntity taxi = TestData.category(2L, "Taxi");

  private void ownerHas(List<ExpenseEntity> expenses) {
    when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(TestData.regularUser()));
    when(expenseRepository.findAllByUser_IdOrderByDateDesc(USER_ID)).thenReturn(expenses);
  }

  @Test
  void emptyHistoryGivesZeroCountAndEmptyMaps() {
    ownerHas(List.of());

    var summary = statisticsService.getSummary(USER_EMAIL);

    assertThat(summary.totalCount()).isZero();
    assertThat(summary.totalByCurrency()).isEmpty();
    assertThat(summary.amountByCategory()).isEmpty();
    assertThat(summary.countByStatus()).isEmpty();
  }

  @Test
  void neverSumsAcrossCurrencies() {
    ownerHas(
        List.of(
            TestData.expense(1L, "10.00", CurrencyType.AZN, StatusType.PAID, food),
            TestData.expense(2L, "5.50", CurrencyType.AZN, StatusType.PENDING, taxi),
            TestData.expense(3L, "100.00", CurrencyType.USD, StatusType.PAID, food)));

    var summary = statisticsService.getSummary(USER_EMAIL);

    assertThat(summary.totalCount()).isEqualTo(3);
    assertThat(summary.totalByCurrency())
        .containsEntry(CurrencyType.AZN, new BigDecimal("15.50"))
        .containsEntry(CurrencyType.USD, new BigDecimal("100.00"))
        .hasSize(2);
    assertThat(summary.amountByCategory().get(CurrencyType.AZN))
        .containsEntry("Food", new BigDecimal("10.00"))
        .containsEntry("Taxi", new BigDecimal("5.50"));
    assertThat(summary.amountByCategory().get(CurrencyType.USD))
        .containsOnlyKeys("Food")
        .containsEntry("Food", new BigDecimal("100.00"));
    assertThat(summary.countByStatus())
        .containsEntry(StatusType.PAID, 2L)
        .containsEntry(StatusType.PENDING, 1L);
  }

  @Test
  void expensesWithoutCurrencyOrStatusAreCountedButNotSummed() {
    ownerHas(List.of(TestData.expense(1L, "7.00", null, null, food)));

    var summary = statisticsService.getSummary(USER_EMAIL);

    assertThat(summary.totalCount()).isEqualTo(1);
    assertThat(summary.totalByCurrency()).isEmpty();
    assertThat(summary.countByStatus()).isEmpty();
  }

  @Test
  void unknownCallerThrowsUserNotFound() {
    when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> statisticsService.getSummary(USER_EMAIL))
        .isInstanceOf(UserNotFoundException.class);
  }
}

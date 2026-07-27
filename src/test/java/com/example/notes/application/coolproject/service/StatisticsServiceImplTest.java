package com.example.notes.application.coolproject.service;

import com.example.notes.application.coolproject.dto.statisticsDto.StatisticsSummaryResponse;
import com.example.notes.application.coolproject.entity.CategoryEntity;
import com.example.notes.application.coolproject.entity.ExpenseEntity;
import com.example.notes.application.coolproject.enums.CategoryType;
import com.example.notes.application.coolproject.enums.CurrencyType;
import com.example.notes.application.coolproject.enums.StatusType;
import com.example.notes.application.coolproject.repository.ExpenseRepository;
import com.example.notes.application.coolproject.service.statistics.StatisticsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceImplTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private StatisticsServiceImpl statisticsService;

    @Test
    void getSummary_emptyExpenses_returnsZeroTotals() {
        when(expenseRepository.findAll()).thenReturn(List.of());

        StatisticsSummaryResponse result = statisticsService.getSummary();

        assertThat(result.totalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totalCount()).isZero();
        assertThat(result.amountByCategory()).isEmpty();
        assertThat(result.countByStatus()).isEmpty();
        assertThat(result.amountByCurrency()).isEmpty();
    }

    @Test
    void getSummary_withExpenses_aggregatesCorrectly() {
        CategoryEntity food = buildCategory("Food");
        CategoryEntity transport = buildCategory("Transport");

        ExpenseEntity e1 = buildExpense(BigDecimal.valueOf(100), StatusType.PAID, CurrencyType.USD, food);
        ExpenseEntity e2 = buildExpense(BigDecimal.valueOf(50), StatusType.PENDING, CurrencyType.USD, food);
        ExpenseEntity e3 = buildExpense(BigDecimal.valueOf(30), StatusType.PAID, CurrencyType.EUR, transport);

        when(expenseRepository.findAll()).thenReturn(List.of(e1, e2, e3));

        StatisticsSummaryResponse result = statisticsService.getSummary();

        assertThat(result.totalCount()).isEqualTo(3);
        assertThat(result.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(180));
        assertThat(result.amountByCategory()).containsKey("Food");
        assertThat(result.amountByCategory().get("Food")).isEqualByComparingTo(BigDecimal.valueOf(150));
        assertThat(result.amountByCategory().get("Transport")).isEqualByComparingTo(BigDecimal.valueOf(30));
        assertThat(result.countByStatus().get(StatusType.PAID)).isEqualTo(2L);
        assertThat(result.countByStatus().get(StatusType.PENDING)).isEqualTo(1L);
        assertThat(result.amountByCurrency().get(CurrencyType.USD)).isEqualByComparingTo(BigDecimal.valueOf(150));
        assertThat(result.amountByCurrency().get(CurrencyType.EUR)).isEqualByComparingTo(BigDecimal.valueOf(30));
    }

    private CategoryEntity buildCategory(String name) {
        CategoryEntity c = new CategoryEntity();
        c.setName(name);
        c.setIcon("icon");
        c.setColor("red");
        c.setCategoryType(CategoryType.EXPENSE);
        return c;
    }

    private ExpenseEntity buildExpense(BigDecimal amount, StatusType status, CurrencyType currency, CategoryEntity category) {
        ExpenseEntity e = new ExpenseEntity();
        e.setAmount(amount);
        e.setDescription("test");
        e.setDate(LocalDate.now());
        e.setStatusType(status);
        e.setCurrencyType(currency);
        e.setCategory(category);
        return e;
    }
}

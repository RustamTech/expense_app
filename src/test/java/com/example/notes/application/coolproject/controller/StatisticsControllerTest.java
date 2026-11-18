package com.example.notes.application.coolproject.controller;

import static com.example.notes.application.coolproject.TestData.USER_EMAIL;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.notes.application.coolproject.dto.statistics.StatisticsSummaryResponse;
import com.example.notes.application.coolproject.enums.CurrencyType;
import com.example.notes.application.coolproject.enums.StatusType;
import com.example.notes.application.coolproject.service.statistics.StatisticsService;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(StatisticsController.class)
class StatisticsControllerTest extends AbstractControllerTest {

  private static final String SUMMARY = "/api/notes/statistics/summary";

  @MockitoBean private StatisticsService statisticsService;

  @Test
  @WithMockUser(username = USER_EMAIL)
  void returnsSummaryOfCallerGroupedByCurrency() throws Exception {
    var summary =
        new StatisticsSummaryResponse(
            2,
            Map.of(CurrencyType.AZN, new BigDecimal("30.00")),
            Map.of(CurrencyType.AZN, Map.of("Food", new BigDecimal("30.00"))),
            Map.of(StatusType.PAID, 2L));
    when(statisticsService.getSummary(USER_EMAIL)).thenReturn(summary);

    mockMvc
        .perform(get(SUMMARY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalCount").value(2))
        .andExpect(jsonPath("$.totalByCurrency.AZN").value(30.00))
        .andExpect(jsonPath("$.amountByCategory.AZN.Food").value(30.00))
        .andExpect(jsonPath("$.countByStatus.PAID").value(2));
  }

  @Test
  void anonymousReturns401() throws Exception {
    mockMvc.perform(get(SUMMARY)).andExpect(status().isUnauthorized());
  }
}

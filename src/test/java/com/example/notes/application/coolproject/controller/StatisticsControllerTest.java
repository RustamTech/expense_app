package com.example.notes.application.coolproject.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.notes.application.coolproject.config.SecurityConfig;
import com.example.notes.application.coolproject.dto.statistics.StatisticsSummaryResponse;
import com.example.notes.application.coolproject.enums.CurrencyType;
import com.example.notes.application.coolproject.enums.StatusType;
import com.example.notes.application.coolproject.security.JwtService;
import com.example.notes.application.coolproject.security.UserDetailsServiceImpl;
import com.example.notes.application.coolproject.service.statistics.StatisticsService;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StatisticsController.class)
@Import(SecurityConfig.class)
class StatisticsControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private StatisticsService statisticsService;

  @MockBean private JwtService jwtService;

  @MockBean private UserDetailsServiceImpl userDetailsService;

  @Test
  @WithMockUser
  void getSummary_returns200WithData() throws Exception {
    StatisticsSummaryResponse response =
        new StatisticsSummaryResponse(
            BigDecimal.valueOf(180),
            3L,
            Map.of("Food", BigDecimal.valueOf(150)),
            Map.of(StatusType.PAID, 2L),
            Map.of(CurrencyType.USD, BigDecimal.valueOf(150)));
    when(statisticsService.getSummary(any())).thenReturn(response);

    mockMvc
        .perform(get("/api/notes/statistics/summary"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalAmount").value(180))
        .andExpect(jsonPath("$.totalCount").value(3));
  }

  @Test
  void getSummary_unauthenticated_returns401() throws Exception {
    mockMvc.perform(get("/api/notes/statistics/summary")).andExpect(status().isUnauthorized());
  }
}

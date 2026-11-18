package com.example.notes.application.coolproject.service.statistics;

import com.example.notes.application.coolproject.dto.statistics.StatisticsSummaryResponse;

public interface StatisticsService {
  StatisticsSummaryResponse getSummary(String email);
}

package com.example.notes.application.coolproject.controller;

import com.example.notes.application.coolproject.config.Endpoints;
import com.example.notes.application.coolproject.dto.statisticsDto.StatisticsSummaryResponse;
import com.example.notes.application.coolproject.service.statistics.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Statistics", description = "Expense statistics and summaries")
@RestController
@RequestMapping(Endpoints.STATISTICS)
@RequiredArgsConstructor
public class StatisticsController {

  private final StatisticsService statisticsService;

  @Operation(summary = "Get expense statistics summary for the current user")
  @GetMapping(Endpoints.STATISTICS_SUMMARY)
  public ResponseEntity<StatisticsSummaryResponse> getSummary(Authentication authentication) {
    return ResponseEntity.ok(statisticsService.getSummary(authentication.getName()));
  }
}

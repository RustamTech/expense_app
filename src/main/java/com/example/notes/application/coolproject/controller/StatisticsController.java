package com.example.notes.application.coolproject.controller;

import com.example.notes.application.coolproject.config.Endpoints;
import com.example.notes.application.coolproject.dto.statistics.StatisticsSummaryResponse;
import com.example.notes.application.coolproject.service.statistics.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Statistics over the authenticated user's own expenses. */
@Tag(name = "Statistics", description = "Expense statistics of the authenticated user")
@RestController
@RequestMapping(Endpoints.STATISTICS)
@RequiredArgsConstructor
public class StatisticsController {

  private final StatisticsService statisticsService;

  /**
   * Returns the spending summary, grouped by currency.
   *
   * @param authentication the caller
   * @return the summary
   */
  @Operation(summary = "Get the expense summary of the current user")
  @ApiResponse(responseCode = "200", description = "Summary")
  @ApiResponse(responseCode = "401", description = "Missing or invalid token")
  @GetMapping(Endpoints.STATISTICS_SUMMARY)
  public ResponseEntity<StatisticsSummaryResponse> getSummary(Authentication authentication) {
    return ResponseEntity.ok(statisticsService.getSummary(authentication.getName()));
  }
}

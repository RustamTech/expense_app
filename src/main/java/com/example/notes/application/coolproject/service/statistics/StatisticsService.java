package com.example.notes.application.coolproject.service.statistics;

import com.example.notes.application.coolproject.dto.statistics.StatisticsSummaryResponse;
import com.example.notes.application.coolproject.exception.custom.UserNotFoundException;

/** Aggregated statistics over the authenticated user's own expenses. */
public interface StatisticsService {

  /**
   * Builds the spending summary of the authenticated user. Amounts are grouped by currency and
   * never summed across currencies.
   *
   * @param email email of the authenticated user (owner)
   * @return the summary; empty maps and zero count if there are no expenses
   * @throws UserNotFoundException if no user exists with {@code email}
   */
  StatisticsSummaryResponse getSummary(String email);
}

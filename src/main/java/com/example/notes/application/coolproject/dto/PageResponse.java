package com.example.notes.application.coolproject.dto;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Stable JSON shape for a page of results (Spring Data's {@code PageImpl} is not a stable API).
 *
 * @param content items of the current page
 * @param page zero-based page index
 * @param size requested page size
 * @param totalElements total number of items
 * @param totalPages total number of pages
 * @param <T> item type
 */
public record PageResponse<T>(
    List<T> content, int page, int size, long totalElements, int totalPages) {

  /**
   * Converts a Spring Data page.
   *
   * @param page the page to convert
   * @param <T> item type
   * @return the response
   */
  public static <T> PageResponse<T> from(Page<T> page) {
    return new PageResponse<>(
        List.copyOf(page.getContent()),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages());
  }
}

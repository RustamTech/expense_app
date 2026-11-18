package com.example.notes.application.coolproject.dto.category;

import com.example.notes.application.coolproject.enums.CategoryType;

/**
 * A catalog category.
 *
 * @param id identifier
 * @param color display color
 * @param icon icon
 * @param name unique name
 * @param categoryType income or expense
 */
public record CategoryResponse(
    Long id, String color, String icon, String name, CategoryType categoryType) {}

package com.example.notes.application.coolproject.dto.category;

import com.example.notes.application.coolproject.enums.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;

public record CategoryUpdateRequest(
    @Schema(description = "Expense amount", example = "15") String icon,
    @Schema(description = "Color of icon", example = "Green") String color,
    @Schema(description = "Name of expense", example = "MC") String name,
    @Schema(description = "Name of category's type", example = "Drinks")
        CategoryType categoryType) {}

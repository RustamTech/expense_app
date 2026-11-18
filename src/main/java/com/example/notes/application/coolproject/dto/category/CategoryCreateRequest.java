package com.example.notes.application.coolproject.dto.category;

import com.example.notes.application.coolproject.enums.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Payload for creating a catalog category (administrators only).
 *
 * @param icon icon shown next to the category
 * @param color display color
 * @param name unique name, case-insensitive
 * @param categoryType whether the category is for income or expenses
 */
public record CategoryCreateRequest(
    @Schema(description = "Icon of the category", example = "🍔") @NotBlank @Size(max = 50) String icon,
    @Schema(description = "Display color", example = "#FF5733") @NotBlank @Size(max = 30) String color,
    @Schema(description = "Unique category name", example = "Food") @NotBlank @Size(max = 50) String name,
    @Schema(description = "Category type", example = "EXPENSE") @NotNull CategoryType categoryType) {}

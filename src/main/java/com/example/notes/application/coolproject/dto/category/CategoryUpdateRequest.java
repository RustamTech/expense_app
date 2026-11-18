package com.example.notes.application.coolproject.dto.category;

import com.example.notes.application.coolproject.dto.NotBlankIfPresent;
import com.example.notes.application.coolproject.enums.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Partial update of a catalog category (administrators only). {@code null} fields are left
 * unchanged; present text fields must not be blank.
 *
 * @param icon new icon
 * @param color new display color
 * @param name new unique name
 * @param categoryType new category type
 */
public record CategoryUpdateRequest(
    @Schema(description = "Icon of the category", example = "🍕")
        @Size(max = 50) @Pattern(regexp = NotBlankIfPresent.PATTERN, message = NotBlankIfPresent.MESSAGE) String icon,
    @Schema(description = "Display color", example = "#3366FF")
        @Size(max = 30) @Pattern(regexp = NotBlankIfPresent.PATTERN, message = NotBlankIfPresent.MESSAGE) String color,
    @Schema(description = "Unique category name", example = "Pizza")
        @Size(max = 50) @Pattern(regexp = NotBlankIfPresent.PATTERN, message = NotBlankIfPresent.MESSAGE) String name,
    @Schema(description = "Category type", example = "EXPENSE") CategoryType categoryType) {}

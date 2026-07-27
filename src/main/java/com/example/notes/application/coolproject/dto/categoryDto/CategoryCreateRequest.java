package com.example.notes.application.coolproject.dto.categoryDto;

import com.example.notes.application.coolproject.enums.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoryCreateRequest(

        @Schema(description = "Expense amount", example = "15")
        @NotBlank
        String icon,

        @Schema(description = "Color of icon", example = "Green")
        @NotBlank
        String color,

        @Schema(description = "Name of expense", example = "MC")
        @NotBlank
        String name,

        @Schema(description = "Name of category's type", example = "Drinks")
        @NotNull
        CategoryType categoryType
) {
}

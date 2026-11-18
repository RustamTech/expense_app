package com.example.notes.application.coolproject;

import com.example.notes.application.coolproject.dto.category.CategoryResponse;
import com.example.notes.application.coolproject.dto.expense.ExpenseResponse;
import com.example.notes.application.coolproject.dto.user.UserResponse;
import com.example.notes.application.coolproject.entity.CategoryEntity;
import com.example.notes.application.coolproject.entity.ExpenseEntity;
import com.example.notes.application.coolproject.entity.UserEntity;
import com.example.notes.application.coolproject.enums.CategoryType;
import com.example.notes.application.coolproject.enums.CurrencyType;
import com.example.notes.application.coolproject.enums.StatusType;
import com.example.notes.application.coolproject.enums.UserRole;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.test.util.ReflectionTestUtils;

/** Shared builders for test entities and DTOs. Entity ids have no setter, so they are injected. */
public final class TestData {

  public static final String USER_EMAIL = "user@example.com";
  public static final Long USER_ID = 1L;
  public static final String ADMIN_EMAIL = "admin@example.com";
  public static final Long ADMIN_ID = 100L;
  public static final LocalDate DAY = LocalDate.of(2026, 1, 14);

  private TestData() {}

  public static UserEntity user(Long id, String email, UserRole role) {
    UserEntity user = new UserEntity();
    ReflectionTestUtils.setField(user, "id", id);
    user.setFirstName("John");
    user.setLastName("Doe");
    user.setEmail(email);
    user.setPhoneNumber("+994501234567");
    user.setPassword("hashed");
    user.setUserRole(role);
    user.setEnabled(true);
    return user;
  }

  public static UserEntity regularUser() {
    return user(USER_ID, USER_EMAIL, UserRole.USER);
  }

  public static CategoryEntity category(Long id, String name) {
    CategoryEntity category = new CategoryEntity();
    ReflectionTestUtils.setField(category, "id", id);
    category.setName(name);
    category.setIcon("🍔");
    category.setColor("#FF0000");
    category.setCategoryType(CategoryType.EXPENSE);
    return category;
  }

  public static ExpenseEntity expense(
      Long id, String amount, CurrencyType currency, StatusType status, CategoryEntity category) {
    ExpenseEntity expense = new ExpenseEntity();
    ReflectionTestUtils.setField(expense, "id", id);
    expense.setAmount(new BigDecimal(amount));
    expense.setDescription("Lunch");
    expense.setDate(DAY);
    expense.setCurrencyType(currency);
    expense.setStatusType(status);
    expense.setCategory(category);
    expense.setUser(regularUser());
    return expense;
  }

  public static CategoryResponse categoryResponse(Long id, String name) {
    return new CategoryResponse(id, "#FF0000", "🍔", name, CategoryType.EXPENSE);
  }

  public static ExpenseResponse expenseResponse(Long id) {
    return new ExpenseResponse(
        id,
        new BigDecimal("12.50"),
        "Lunch",
        null,
        DAY,
        categoryResponse(1L, "Food"),
        StatusType.PAID,
        CurrencyType.AZN,
        LocalDateTime.of(2026, 1, 14, 12, 0),
        LocalDateTime.of(2026, 1, 14, 12, 0));
  }

  public static UserResponse userResponse(Long id, String email, UserRole role) {
    return new UserResponse(
        id, "John", "Doe", email, "+994501234567", role, true, LocalDateTime.of(2026, 1, 1, 0, 0));
  }
}

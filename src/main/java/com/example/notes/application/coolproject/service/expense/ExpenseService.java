package com.example.notes.application.coolproject.service.expense;

import com.example.notes.application.coolproject.dto.expense.ExpenseCreateRequest;
import com.example.notes.application.coolproject.dto.expense.ExpenseResponse;
import com.example.notes.application.coolproject.dto.expense.ExpenseUpdateRequest;
import com.example.notes.application.coolproject.enums.StatusType;
import com.example.notes.application.coolproject.exception.custom.CategoryNotFoundException;
import com.example.notes.application.coolproject.exception.custom.ExpenseNotFoundException;
import com.example.notes.application.coolproject.exception.custom.UserNotFoundException;
import java.util.List;

/**
 * Expenses of the authenticated user. Every operation is scoped to the owner: an expense of another
 * user behaves exactly like a missing one. Lists are sorted newest first.
 */
public interface ExpenseService {

  /**
   * Returns one own expense.
   *
   * @param email email of the authenticated user (owner)
   * @param id expense identifier
   * @return the expense
   * @throws UserNotFoundException if no user exists with {@code email}
   * @throws ExpenseNotFoundException if the expense does not exist or belongs to another user
   */
  ExpenseResponse findById(String email, Long id);

  /**
   * Returns all own expenses.
   *
   * @param email email of the authenticated user (owner)
   * @return the expenses
   * @throws UserNotFoundException if no user exists with {@code email}
   */
  List<ExpenseResponse> findAll(String email);

  /**
   * Returns own expenses with the given status.
   *
   * @param email email of the authenticated user (owner)
   * @param statusType status to filter by
   * @return the matching expenses
   * @throws UserNotFoundException if no user exists with {@code email}
   */
  List<ExpenseResponse> findAllByStatus(String email, StatusType statusType);

  /**
   * Returns own expenses in the given category.
   *
   * @param email email of the authenticated user (owner)
   * @param categoryId category identifier
   * @return the matching expenses
   * @throws UserNotFoundException if no user exists with {@code email}
   * @throws CategoryNotFoundException if the category does not exist
   */
  List<ExpenseResponse> findAllByCategory(String email, Long categoryId);

  /**
   * Deletes one own expense.
   *
   * @param email email of the authenticated user (owner)
   * @param id expense identifier
   * @throws UserNotFoundException if no user exists with {@code email}
   * @throws ExpenseNotFoundException if the expense does not exist or belongs to another user
   */
  void delete(String email, Long id);

  /**
   * Changes the non-null fields of one own expense.
   *
   * @param email email of the authenticated user (owner)
   * @param id expense identifier
   * @param request partial update payload
   * @return the updated expense
   * @throws UserNotFoundException if no user exists with {@code email}
   * @throws ExpenseNotFoundException if the expense does not exist or belongs to another user
   * @throws CategoryNotFoundException if a new category id is given and does not exist
   */
  ExpenseResponse update(String email, Long id, ExpenseUpdateRequest request);

  /**
   * Creates an expense owned by the authenticated user.
   *
   * @param email email of the authenticated user (future owner)
   * @param request create payload
   * @return the created expense
   * @throws UserNotFoundException if no user exists with {@code email}
   * @throws CategoryNotFoundException if the category does not exist
   */
  ExpenseResponse create(String email, ExpenseCreateRequest request);
}

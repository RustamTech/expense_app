package com.example.notes.application.coolproject.service.admin;

import com.example.notes.application.coolproject.dto.PageResponse;
import com.example.notes.application.coolproject.dto.user.UserResponse;
import com.example.notes.application.coolproject.enums.UserRole;
import com.example.notes.application.coolproject.exception.custom.OperationNotAllowedException;
import com.example.notes.application.coolproject.exception.custom.UserNotFoundException;
import org.springframework.data.domain.Pageable;

/**
 * User management for administrators. An administrator can never change the role or status of, or
 * delete, their own account, so the system cannot be left without an administrator by accident.
 */
public interface AdminUserService {

  /**
   * Lists all accounts.
   *
   * @param pageable page, size and sort
   * @return one page of accounts
   */
  PageResponse<UserResponse> findAll(Pageable pageable);

  /**
   * Returns one account.
   *
   * @param id account identifier
   * @return the account
   * @throws UserNotFoundException if no account has this id
   */
  UserResponse findById(Long id);

  /**
   * Changes the role of an account.
   *
   * @param actorEmail email of the administrator performing the change
   * @param id account identifier
   * @param role new role
   * @return the updated account
   * @throws UserNotFoundException if no account has this id
   * @throws OperationNotAllowedException if the administrator targets their own account
   */
  UserResponse changeRole(String actorEmail, Long id, UserRole role);

  /**
   * Blocks or unblocks an account.
   *
   * @param actorEmail email of the administrator performing the change
   * @param id account identifier
   * @param enabled {@code false} to block
   * @return the updated account
   * @throws UserNotFoundException if no account has this id
   * @throws OperationNotAllowedException if the administrator targets their own account
   */
  UserResponse changeStatus(String actorEmail, Long id, boolean enabled);

  /**
   * Deletes an account together with all its expenses.
   *
   * @param actorEmail email of the administrator performing the deletion
   * @param id account identifier
   * @throws UserNotFoundException if no account has this id
   * @throws OperationNotAllowedException if the administrator targets their own account
   */
  void delete(String actorEmail, Long id);
}

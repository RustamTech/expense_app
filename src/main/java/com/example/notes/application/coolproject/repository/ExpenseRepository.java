package com.example.notes.application.coolproject.repository;

import com.example.notes.application.coolproject.entity.ExpenseEntity;
import com.example.notes.application.coolproject.enums.StatusType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Data access for expenses. Every read is scoped to an owner id, so a query can never return
 * another user's expense. List queries fetch the category eagerly to avoid N+1 selects.
 */
public interface ExpenseRepository extends JpaRepository<ExpenseEntity, Long> {

  /**
   * Finds one expense of the given owner.
   *
   * @param id expense identifier
   * @param userId owner identifier
   * @return the expense, or empty if it does not exist or belongs to someone else
   */
  @EntityGraph(attributePaths = "category")
  Optional<ExpenseEntity> findByIdAndUser_Id(Long id, Long userId);

  /**
   * Returns all expenses of the owner, newest first.
   *
   * @param userId owner identifier
   * @return the expenses
   */
  @EntityGraph(attributePaths = "category")
  List<ExpenseEntity> findAllByUser_IdOrderByDateDesc(Long userId);

  /**
   * Returns the owner's expenses with the given status, newest first.
   *
   * @param userId owner identifier
   * @param statusType status to filter by
   * @return the matching expenses
   */
  @EntityGraph(attributePaths = "category")
  List<ExpenseEntity> findAllByUser_IdAndStatusTypeOrderByDateDesc(
      Long userId, StatusType statusType);

  /**
   * Returns the owner's expenses in the given category, newest first.
   *
   * @param userId owner identifier
   * @param categoryId category identifier
   * @return the matching expenses
   */
  @EntityGraph(attributePaths = "category")
  List<ExpenseEntity> findAllByUser_IdAndCategory_IdOrderByDateDesc(Long userId, Long categoryId);

  /**
   * Checks whether any expense (of any user) references the category.
   *
   * @param categoryId category identifier
   * @return {@code true} if the category is in use
   */
  boolean existsByCategory_Id(Long categoryId);

  /**
   * Deletes every expense of a user in one statement; used before deleting the account.
   *
   * @param userId owner identifier
   * @return number of deleted expenses
   */
  @Modifying
  @Query("DELETE FROM ExpenseEntity e WHERE e.user.id = :userId")
  int deleteAllByUserId(@Param("userId") Long userId);
}

package com.example.notes.application.coolproject.repository;

import com.example.notes.application.coolproject.entity.CategoryEntity;
import com.example.notes.application.coolproject.entity.ExpenseEntity;
import com.example.notes.application.coolproject.enums.CurrencyType;
import com.example.notes.application.coolproject.enums.StatusType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<ExpenseEntity, Long> {

  List<ExpenseEntity> findAllByStatusType(StatusType statusType);

  List<ExpenseEntity> findAllByCurrencyType(CurrencyType currencyType);

  List<ExpenseEntity> findAllByCategoryAndDateBetween(
      CategoryEntity category, LocalDate dateFrom, LocalDate dateTo);

  List<ExpenseEntity> findAllByCategory_Id(Long categoryId);

  Optional<ExpenseEntity> findByIdAndUser_Id(Long id, Long userId);

  List<ExpenseEntity> findAllByUser_Id(Long userId);

  List<ExpenseEntity> findAllByUser_IdAndStatusType(Long userId, StatusType statusType);

  List<ExpenseEntity> findAllByUser_IdAndCategory_Id(Long userId, Long categoryId);

  @Query("SELECT SUM(e.amount) FROM ExpenseEntity e WHERE e.date BETWEEN :dateFrom AND :dateTo")
  BigDecimal sumAmountByDateBetween(
      @Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo);
}

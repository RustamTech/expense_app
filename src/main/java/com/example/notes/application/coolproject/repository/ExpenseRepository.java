package com.example.notes.application.coolproject.repository;

import com.example.notes.application.coolproject.entity.CategoryEntity;
import com.example.notes.application.coolproject.entity.ExpenseEntity;
import com.example.notes.application.coolproject.enums.CurrencyType;
import com.example.notes.application.coolproject.enums.StatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<ExpenseEntity, Long> {

    List<ExpenseEntity> findAllByStatusType(StatusType statusType);

    List<ExpenseEntity> findAllByCurrencyType(CurrencyType currencyType);

    List<ExpenseEntity> findAllByCategoryAndDateBetween(CategoryEntity category, LocalDate dateFrom, LocalDate dateTo);

    List<ExpenseEntity> findAllByCategory_Id(Long categoryId);

    @Query("SELECT SUM(e.amount) FROM ExpenseEntity e WHERE e.date BETWEEN :dateFrom AND :dateTo")
    BigDecimal sumAmountByDateBetween(@Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo);
}
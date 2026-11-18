package com.example.notes.application.coolproject.entity;

import com.example.notes.application.coolproject.enums.CurrencyType;
import com.example.notes.application.coolproject.enums.StatusType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/** A single expense, owned by exactly one user and assigned to one catalog category. */
@Entity
@Table(name = "expenses")
@Getter
@Setter
public class ExpenseEntity extends BaseEntity {

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false)
  private String description;

  @Column(length = 1000)
  private String note;

  @Column(nullable = false)
  private LocalDate date;

  @Enumerated(EnumType.STRING)
  private StatusType statusType;

  @Enumerated(EnumType.STRING)
  private CurrencyType currencyType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id", nullable = false)
  private CategoryEntity category;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;
}

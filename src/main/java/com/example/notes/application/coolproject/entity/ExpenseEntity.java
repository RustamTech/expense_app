package com.example.notes.application.coolproject.entity;

import com.example.notes.application.coolproject.enums.CurrencyType;
import com.example.notes.application.coolproject.enums.StatusType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "expenses")
@Getter
@Setter
public class ExpenseEntity extends BaseEntity {

  @Column(nullable = false)
  private BigDecimal amount;

  @Column(nullable = false)
  private String description;

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

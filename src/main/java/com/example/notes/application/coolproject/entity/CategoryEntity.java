package com.example.notes.application.coolproject.entity;

import com.example.notes.application.coolproject.enums.CategoryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "categories")
@Getter
@Setter
public class CategoryEntity extends BaseEntity {

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String icon;

  @Column(nullable = false)
  private String color;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CategoryType categoryType;

  @Column(nullable = false)
  private boolean defaultCategory;
}

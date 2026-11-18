package com.example.notes.application.coolproject.entity;

import com.example.notes.application.coolproject.enums.CategoryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * A category from the shared catalog. Every user can read categories; only administrators can
 * create, change or delete them.
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
public class CategoryEntity extends BaseEntity {

  @Column(nullable = false, unique = true, length = 50)
  private String name;

  @Column(nullable = false, length = 50)
  private String icon;

  @Column(nullable = false, length = 30)
  private String color;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CategoryType categoryType;

  @Column(nullable = false)
  private boolean defaultCategory;
}

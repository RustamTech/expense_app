package com.example.notes.application.coolproject.repository;

import com.example.notes.application.coolproject.entity.CategoryEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for the shared category catalog. */
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

  /**
   * Checks whether a category with this name exists, ignoring case.
   *
   * @param name category name
   * @return {@code true} if the name is taken
   */
  boolean existsByNameIgnoreCase(String name);

  /**
   * Checks whether another category already uses this name, ignoring case.
   *
   * @param name category name
   * @param id identifier of the category being renamed
   * @return {@code true} if a different category has this name
   */
  boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

  /**
   * Returns the whole catalog sorted by name.
   *
   * @return all categories
   */
  List<CategoryEntity> findAllByOrderByNameAsc();
}

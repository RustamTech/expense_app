package com.example.notes.application.coolproject.repository;

import com.example.notes.application.coolproject.entity.CategoryEntity;
import com.example.notes.application.coolproject.enums.CategoryType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

  List<CategoryEntity> findAllByCategoryType(CategoryType categoryType);

  Optional<CategoryEntity> findByName(String name);

  List<CategoryEntity> findAllByDefaultCategory(boolean defaultCategory);

  boolean existsByName(String name);
}

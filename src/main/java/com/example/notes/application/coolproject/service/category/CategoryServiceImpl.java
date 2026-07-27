package com.example.notes.application.coolproject.service.category;

import com.example.notes.application.coolproject.dto.categoryDto.CategoryCreateRequest;
import com.example.notes.application.coolproject.dto.categoryDto.CategoryResponse;
import com.example.notes.application.coolproject.dto.categoryDto.CategoryUpdateRequest;
import com.example.notes.application.coolproject.entity.CategoryEntity;
import com.example.notes.application.coolproject.exception.custom.CategoryNotFoundException;
import com.example.notes.application.coolproject.exception.custom.ResourceAlreadyExistsException;
import com.example.notes.application.coolproject.mapper.CategoryMapper;
import com.example.notes.application.coolproject.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse findById(Long id){
        log.info("Fetching category by id: {}", + id);
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Category not found with id: {}", id);
                    return new CategoryNotFoundException(id);
                });
        log.info("Succesfully fetching category by id: {}", id);
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse create(CategoryCreateRequest request) {
        log.info("Creating category with name: {}", request.name());

        categoryRepository.findByName(request.name())
                .ifPresent(c -> {
                    throw new ResourceAlreadyExistsException(request.name());
                });

        CategoryEntity entity = categoryMapper.toEntity(request);
        CategoryEntity saved = categoryRepository.save(entity);
        log.info("Successfully created category with id: {}", saved.getId());
        return categoryMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id){
        log.info("Deleting user with this id: {}", id);
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Category already deleted by id: {}", id);
                    return new CategoryNotFoundException(id);
                });
        categoryRepository.delete(category);
        log.info("Successfully deleted category with this id: {}", id);
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, CategoryUpdateRequest request){
        log.info("Updating category with id: {}", id);
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        categoryMapper.updateCategory(request, category);
        CategoryEntity updated = categoryRepository.save(category);
        log.info("Succesfully updated the category with this id: {}", id);
        return categoryMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll(){
        log.info("Fethcing all categories");
        List<CategoryResponse> categories = categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
        log.info("Succesfully fetched all category");
        return categories;
    }
}

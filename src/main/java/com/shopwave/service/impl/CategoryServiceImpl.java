package com.shopwave.service.impl;

import com.shopwave.dto.CategoryDto;
import com.shopwave.exception.ResourceNotFoundException;
import com.shopwave.mapper.CategoryMapper;
import com.shopwave.model.Category;
import com.shopwave.repository.CategoryRepository;
import com.shopwave.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryDto create(CategoryDto categoryDto) {
        Category category = categoryMapper.toEntity(categoryDto);
        if (categoryDto.getParentId() != null) {
            Category parent = categoryRepository.findById(categoryDto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with id: " + categoryDto.getParentId()));
            category.setParent(parent);
        }
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toDto(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getById(Long id) {
        return categoryRepository.findById(id)
                .map(categoryMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getAll() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public CategoryDto update(Long id, CategoryDto categoryDto) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        Category updatedCategory = categoryMapper.toEntity(categoryDto);
        updatedCategory.setId(existingCategory.getId());
        updatedCategory.setCreatedAt(existingCategory.getCreatedAt());

        if (categoryDto.getParentId() != null) {
            Category parent = categoryRepository.findById(categoryDto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with id: " + categoryDto.getParentId()));
            updatedCategory.setParent(parent);
        }

        Category savedCategory = categoryRepository.save(updatedCategory);
        return categoryMapper.toDto(savedCategory);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        
        if (!category.getSubCategories().isEmpty()) {
            throw new IllegalStateException("Cannot delete category with subcategories");
        }
        
        categoryRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getRootCategories() {
        return categoryRepository.findByParentIsNull().stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getSubCategories(Long parentId) {
        return categoryRepository.findByParentId(parentId).stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategoryTree() {
        List<Category> rootCategories = categoryRepository.findByParentIsNull();
        return buildCategoryTree(rootCategories);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }

    @Override
    @Transactional
    public void moveCategory(Long categoryId, Long newParentId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        Category newParent = null;
        if (newParentId != null) {
            newParent = categoryRepository.findById(newParentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with id: " + newParentId));
        }

        category.setParent(newParent);
        categoryRepository.save(category);
    }

    private List<CategoryDto> buildCategoryTree(List<Category> categories) {
        return categories.stream()
                .map(category -> {
                    CategoryDto dto = categoryMapper.toDto(category);
                    if (!category.getSubCategories().isEmpty()) {
                        dto.setSubCategories(buildCategoryTree(category.getSubCategories().stream().toList()));
                    }
                    return dto;
                })
                .toList();
    }
} 
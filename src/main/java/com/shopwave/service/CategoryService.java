package com.shopwave.service;

import com.shopwave.dto.CategoryDto;
import com.shopwave.model.Category;

import java.util.List;

public interface CategoryService extends BaseService<Category, CategoryDto> {
    List<CategoryDto> getRootCategories();
    List<CategoryDto> getSubCategories(Long parentId);
    List<CategoryDto> getCategoryTree();
    boolean existsByName(String name);
    void moveCategory(Long categoryId, Long newParentId);
} 
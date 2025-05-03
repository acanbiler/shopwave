package com.shopwave.controller;

import com.shopwave.dto.CategoryDto;
import com.shopwave.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@RequestBody CategoryDto categoryDto) {
        return ResponseEntity.ok(categoryService.create(categoryDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable Long id, @RequestBody CategoryDto categoryDto) {
        return ResponseEntity.ok(categoryService.update(id, categoryDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/root")
    public ResponseEntity<List<CategoryDto>> getRootCategories() {
        return ResponseEntity.ok(categoryService.getRootCategories());
    }

    @GetMapping("/{parentId}/subcategories")
    public ResponseEntity<List<CategoryDto>> getSubCategories(@PathVariable Long parentId) {
        return ResponseEntity.ok(categoryService.getSubCategories(parentId));
    }

    @GetMapping("/tree")
    public ResponseEntity<List<CategoryDto>> getCategoryTree() {
        return ResponseEntity.ok(categoryService.getCategoryTree());
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkCategoryExists(@RequestParam String name) {
        return ResponseEntity.ok(categoryService.existsByName(name));
    }

    @PutMapping("/{categoryId}/move")
    public ResponseEntity<Void> moveCategory(
            @PathVariable Long categoryId,
            @RequestParam(required = false) Long newParentId) {
        categoryService.moveCategory(categoryId, newParentId);
        return ResponseEntity.ok().build();
    }
} 
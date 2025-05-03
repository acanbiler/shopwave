package com.shopwave.controller;

import com.shopwave.dto.ProductDto;
import com.shopwave.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto productDto) {
        return ResponseEntity.ok(productService.create(productDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, @RequestBody ProductDto productDto) {
        return ResponseEntity.ok(productService.update(id, productDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductDto>> getProductsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(productService.findByCategoryId(categoryId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductDto>> searchProducts(@RequestParam String query) {
        return ResponseEntity.ok(productService.searchProducts(query));
    }

    @GetMapping("/price-range")
    public ResponseEntity<List<ProductDto>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        return ResponseEntity.ok(productService.findByPriceRange(minPrice, maxPrice));
    }

    @GetMapping("/active")
    public ResponseEntity<List<ProductDto>> getActiveProducts() {
        return ResponseEntity.ok(productService.findActiveProducts());
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<Void> updateStockQuantity(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        productService.updateStockQuantity(id, quantity);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/discount")
    public ResponseEntity<Void> applyDiscount(
            @PathVariable Long id,
            @RequestParam BigDecimal discount) {
        productService.applyDiscount(id, discount);
        return ResponseEntity.ok().build();
    }
} 
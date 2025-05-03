package com.shopwave.service.impl;

import com.shopwave.dto.ProductDto;
import com.shopwave.exception.ResourceNotFoundException;
import com.shopwave.mapper.ProductMapper;
import com.shopwave.model.Product;
import com.shopwave.repository.ProductRepository;
import com.shopwave.repository.ReviewRepository;
import com.shopwave.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductDto create(ProductDto productDto) {
        Product product = productMapper.toEntity(productDto);
        Product savedProduct = productRepository.save(product);
        return enrichProductDto(productMapper.toDto(savedProduct));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getById(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toDto)
                .map(this::enrichProductDto)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getAll() {
        return productRepository.findAll().stream()
                .map(productMapper::toDto)
                .map(this::enrichProductDto)
                .toList();
    }

    @Override
    @Transactional
    public ProductDto update(Long id, ProductDto productDto) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        Product updatedProduct = productMapper.toEntity(productDto);
        updatedProduct.setId(existingProduct.getId());
        updatedProduct.setCreatedAt(existingProduct.getCreatedAt());

        Product savedProduct = productRepository.save(updatedProduct);
        return enrichProductDto(productMapper.toDto(savedProduct));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> findByCategoryId(Long categoryId) {
        return productRepository.findByCategoryId(categoryId).stream()
                .map(productMapper::toDto)
                .map(this::enrichProductDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> searchProducts(String query) {
        return productRepository.searchProducts(query).stream()
                .map(productMapper::toDto)
                .map(this::enrichProductDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice).stream()
                .map(productMapper::toDto)
                .map(this::enrichProductDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> findActiveProducts() {
        return productRepository.findByIsActiveTrue().stream()
                .map(productMapper::toDto)
                .map(this::enrichProductDto)
                .toList();
    }

    @Override
    @Transactional
    public void updateStockQuantity(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        product.setStockQuantity(quantity);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void applyDiscount(Long productId, BigDecimal discount) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        product.setDiscount(discount);
        productRepository.save(product);
    }

    private ProductDto enrichProductDto(ProductDto productDto) {
        Product product = productRepository.findById(productDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productDto.getId()));
        
        productDto.setAverageRating(reviewRepository.getAverageRating(product));
        productDto.setReviewCount(reviewRepository.getReviewCount(product));
        
        return productDto;
    }
} 
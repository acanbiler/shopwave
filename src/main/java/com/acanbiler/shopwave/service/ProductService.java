package com.acanbiler.shopwave.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acanbiler.shopwave.entity.Product;
import com.acanbiler.shopwave.entity.Review;
import com.acanbiler.shopwave.repository.ProductRepository;
import com.acanbiler.shopwave.repository.ReviewRepository;

/**
 * Product management service for ShopWave application.
 * 
 * This service handles product CRUD operations, inventory management,
 * review aggregation, and product-related business logic.
 */
@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;

    public ProductService(ProductRepository productRepository, ReviewRepository reviewRepository) {
        this.productRepository = productRepository;
        this.reviewRepository = reviewRepository;
    }

    /**
     * Find product by ID.
     * 
     * @param id product ID
     * @return product if found
     */
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id)
            .filter(Product::getEnabled);
    }

    /**
     * Find product by SKU.
     * 
     * @param sku product SKU
     * @return product if found
     */
    public Optional<Product> findBySku(String sku) {
        return productRepository.findBySku(sku)
            .filter(Product::getEnabled);
    }

    /**
     * Get all products with pagination.
     * 
     * @param pageable pagination parameters
     * @return page of enabled products
     */
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findByEnabled(true, pageable);
    }

    /**
     * Search products by keyword.
     * 
     * @param keyword search keyword
     * @param pageable pagination parameters
     * @return page of matching products
     */
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        return productRepository.searchProducts(keyword, pageable);
    }

    /**
     * Get products by category.
     * 
     * @param category product category
     * @param pageable pagination parameters
     * @return page of products in category
     */
    public Page<Product> findByCategory(Product.ProductCategory category, Pageable pageable) {
        return productRepository.findByCategory(category, pageable);
    }

    /**
     * Get products by price range.
     * 
     * @param minPrice minimum price
     * @param maxPrice maximum price
     * @param pageable pagination parameters
     * @return page of products in price range
     */
    public Page<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return productRepository.findByPriceRangeAndEnabled(minPrice, maxPrice, pageable);
    }

    /**
     * Get products with filters.
     * 
     * @param category product category (optional)
     * @param brand product brand (optional)
     * @param minPrice minimum price (optional)
     * @param maxPrice maximum price (optional)
     * @param inStockOnly show only in-stock products
     * @param pageable pagination parameters
     * @return page of filtered products
     */
    public Page<Product> findWithFilters(Product.ProductCategory category, String brand,
                                       BigDecimal minPrice, BigDecimal maxPrice,
                                       boolean inStockOnly, Pageable pageable) {
        return productRepository.findProductsWithFilters(category, brand, minPrice, maxPrice, inStockOnly, pageable);
    }

    /**
     * Get featured products.
     * 
     * @param pageable pagination parameters
     * @return page of featured products
     */
    public Page<Product> findFeaturedProducts(Pageable pageable) {
        return productRepository.findByFeaturedAndEnabled(true, true, pageable);
    }

    /**
     * Get new products.
     * 
     * @param days days back to consider as new
     * @param pageable pagination parameters
     * @return page of new products
     */
    public Page<Product> findNewProducts(int days, Pageable pageable) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return productRepository.findNewProducts(since, pageable);
    }

    /**
     * Get top-rated products.
     * 
     * @param minReviews minimum number of reviews required
     * @param pageable pagination parameters
     * @return page of top-rated products
     */
    public Page<Object[]> findTopRatedProducts(long minReviews, Pageable pageable) {
        return productRepository.findTopRatedProducts(minReviews, pageable);
    }

    /**
     * Get most reviewed products.
     * 
     * @param pageable pagination parameters
     * @return page of most reviewed products
     */
    public Page<Object[]> findMostReviewedProducts(Pageable pageable) {
        return productRepository.findMostReviewedProducts(pageable);
    }

    /**
     * Get popular products (based on reviews).
     * 
     * @param pageable pagination parameters
     * @return page of popular products
     */
    public Page<Product> findPopularProducts(Pageable pageable) {
        return productRepository.findPopularProducts(pageable);
    }

    /**
     * Create a new product (admin only).
     * 
     * @param productRequest product creation request
     * @return created product
     */
    @PreAuthorize("hasRole('ADMIN')")
    public Product createProduct(ProductCreateRequest productRequest) {
        if (productRequest.getSku() != null && productRepository.existsBySku(productRequest.getSku())) {
            throw new RuntimeException("SKU already exists");
        }

        Product product = Product.builder()
            .name(productRequest.getName())
            .description(productRequest.getDescription())
            .price(productRequest.getPrice())
            .stockQuantity(productRequest.getStockQuantity())
            .category(productRequest.getCategory())
            .imageUrl(productRequest.getImageUrl())
            .sku(productRequest.getSku())
            .brand(productRequest.getBrand())
            .weight(productRequest.getWeight())
            .dimensions(productRequest.getDimensions())
            .enabled(productRequest.getEnabled() != null ? productRequest.getEnabled() : true)
            .featured(productRequest.getFeatured() != null ? productRequest.getFeatured() : false)
            .build();

        return productRepository.save(product);
    }

    /**
     * Update product (admin only).
     * 
     * @param id product ID
     * @param updateRequest update request
     * @return updated product
     */
    @PreAuthorize("hasRole('ADMIN')")
    public Product updateProduct(Long id, ProductUpdateRequest updateRequest) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        // Check SKU uniqueness if changed
        if (updateRequest.getSku() != null && !updateRequest.getSku().equals(product.getSku())) {
            if (productRepository.existsBySku(updateRequest.getSku())) {
                throw new RuntimeException("SKU already exists");
            }
        }

        // Update fields if provided
        if (updateRequest.getName() != null) {
            product.setName(updateRequest.getName());
        }
        if (updateRequest.getDescription() != null) {
            product.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getPrice() != null) {
            product.setPrice(updateRequest.getPrice());
        }
        if (updateRequest.getStockQuantity() != null) {
            product.setStockQuantity(updateRequest.getStockQuantity());
        }
        if (updateRequest.getCategory() != null) {
            product.setCategory(updateRequest.getCategory());
        }
        if (updateRequest.getImageUrl() != null) {
            product.setImageUrl(updateRequest.getImageUrl());
        }
        if (updateRequest.getSku() != null) {
            product.setSku(updateRequest.getSku());
        }
        if (updateRequest.getBrand() != null) {
            product.setBrand(updateRequest.getBrand());
        }
        if (updateRequest.getWeight() != null) {
            product.setWeight(updateRequest.getWeight());
        }
        if (updateRequest.getDimensions() != null) {
            product.setDimensions(updateRequest.getDimensions());
        }
        if (updateRequest.getEnabled() != null) {
            product.setEnabled(updateRequest.getEnabled());
        }
        if (updateRequest.getFeatured() != null) {
            product.setFeatured(updateRequest.getFeatured());
        }

        return productRepository.save(product);
    }

    /**
     * Delete product (admin only).
     * 
     * @param id product ID
     */
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        productRepository.delete(product);
    }

    /**
     * Update product stock.
     * 
     * @param id product ID
     * @param quantity quantity to add (positive) or remove (negative)
     * @return updated product
     */
    @PreAuthorize("hasRole('ADMIN')")
    public Product updateStock(Long id, int quantity) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        if (quantity > 0) {
            product.increaseStock(quantity);
        } else if (quantity < 0) {
            product.decreaseStock(Math.abs(quantity));
        }

        return productRepository.save(product);
    }

    /**
     * Reserve product stock for purchase.
     * 
     * @param id product ID
     * @param quantity quantity to reserve
     * @return true if successfully reserved
     */
    public boolean reserveStock(Long id, int quantity) {
        int updated = productRepository.decreaseStock(id, quantity);
        return updated > 0;
    }

    /**
     * Release reserved stock (e.g., when payment fails).
     * 
     * @param id product ID
     * @param quantity quantity to release
     */
    public void releaseStock(Long id, int quantity) {
        productRepository.increaseStock(id, quantity);
    }

    /**
     * Set product featured status (admin only).
     * 
     * @param id product ID
     * @param featured featured status
     */
    @PreAuthorize("hasRole('ADMIN')")
    public void setFeatured(Long id, boolean featured) {
        productRepository.updateProductFeaturedStatus(id, featured);
    }

    /**
     * Enable or disable product (admin only).
     * 
     * @param id product ID
     * @param enabled enabled status
     */
    @PreAuthorize("hasRole('ADMIN')")
    public void setEnabled(Long id, boolean enabled) {
        productRepository.updateProductEnabledStatus(id, enabled);
    }

    /**
     * Get low stock products (admin only).
     * 
     * @param threshold stock threshold
     * @return list of low stock products
     */
    @PreAuthorize("hasRole('ADMIN')")
    public List<Product> getLowStockProducts(int threshold) {
        return productRepository.findLowStockProducts(threshold);
    }

    /**
     * Get out of stock products (admin only).
     * 
     * @return list of out of stock products
     */
    @PreAuthorize("hasRole('ADMIN')")
    public List<Product> getOutOfStockProducts() {
        return productRepository.findOutOfStockProducts();
    }

    /**
     * Get all product brands.
     * 
     * @return list of unique brands
     */
    public List<String> getAllBrands() {
        return productRepository.findAllBrands();
    }

    /**
     * Get product statistics (admin only).
     * 
     * @return product statistics
     */
    @PreAuthorize("hasRole('ADMIN')")
    public ProductStatistics getProductStatistics() {
        Object stats = productRepository.getProductStatistics();
        List<Object[]> categoryStats = productRepository.getProductCountByCategory();
        List<Object[]> monthlyStats = productRepository.getMonthlyProductCreationStats(LocalDateTime.now().minusMonths(12));

        return ProductStatistics.builder()
            .totalProducts(extractLong(stats, 0))
            .inStockProducts(extractLong(stats, 1))
            .outOfStockProducts(extractLong(stats, 2))
            .lowStockProducts(extractLong(stats, 3))
            .averagePrice(extractBigDecimal(stats, 4))
            .minPrice(extractBigDecimal(stats, 5))
            .maxPrice(extractBigDecimal(stats, 6))
            .categoryDistribution(categoryStats)
            .monthlyCreations(monthlyStats)
            .build();
    }

    /**
     * Get product reviews.
     * 
     * @param productId product ID
     * @param pageable pagination parameters
     * @return page of approved reviews
     */
    public Page<Review> getProductReviews(Long productId, Pageable pageable) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        return reviewRepository.findApprovedReviewsByProduct(product, pageable);
    }

    /**
     * Get product review statistics.
     * 
     * @param productId product ID
     * @return review statistics
     */
    public Object getProductReviewStatistics(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        return reviewRepository.getProductReviewStatistics(product);
    }

    /**
     * Extract long value from statistics object array.
     */
    private Long extractLong(Object stats, int index) {
        if (stats instanceof Object[]) {
            Object[] array = (Object[]) stats;
            if (array.length > index && array[index] != null) {
                return ((Number) array[index]).longValue();
            }
        }
        return 0L;
    }

    /**
     * Extract BigDecimal value from statistics object array.
     */
    private BigDecimal extractBigDecimal(Object stats, int index) {
        if (stats instanceof Object[]) {
            Object[] array = (Object[]) stats;
            if (array.length > index && array[index] != null) {
                return (BigDecimal) array[index];
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * Product creation request DTO.
     */
    public static class ProductCreateRequest {
        private String name;
        private String description;
        private BigDecimal price;
        private Integer stockQuantity;
        private Product.ProductCategory category;
        private String imageUrl;
        private String sku;
        private String brand;
        private BigDecimal weight;
        private String dimensions;
        private Boolean enabled;
        private Boolean featured;

        // Constructors
        public ProductCreateRequest() {}

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        
        public Integer getStockQuantity() { return stockQuantity; }
        public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
        
        public Product.ProductCategory getCategory() { return category; }
        public void setCategory(Product.ProductCategory category) { this.category = category; }
        
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        
        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }
        
        public BigDecimal getWeight() { return weight; }
        public void setWeight(BigDecimal weight) { this.weight = weight; }
        
        public String getDimensions() { return dimensions; }
        public void setDimensions(String dimensions) { this.dimensions = dimensions; }
        
        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }
        
        public Boolean getFeatured() { return featured; }
        public void setFeatured(Boolean featured) { this.featured = featured; }
    }

    /**
     * Product update request DTO.
     */
    public static class ProductUpdateRequest {
        private String name;
        private String description;
        private BigDecimal price;
        private Integer stockQuantity;
        private Product.ProductCategory category;
        private String imageUrl;
        private String sku;
        private String brand;
        private BigDecimal weight;
        private String dimensions;
        private Boolean enabled;
        private Boolean featured;

        // Constructors
        public ProductUpdateRequest() {}

        // Getters and setters (same as create request)
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        
        public Integer getStockQuantity() { return stockQuantity; }
        public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
        
        public Product.ProductCategory getCategory() { return category; }
        public void setCategory(Product.ProductCategory category) { this.category = category; }
        
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        
        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }
        
        public BigDecimal getWeight() { return weight; }
        public void setWeight(BigDecimal weight) { this.weight = weight; }
        
        public String getDimensions() { return dimensions; }
        public void setDimensions(String dimensions) { this.dimensions = dimensions; }
        
        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }
        
        public Boolean getFeatured() { return featured; }
        public void setFeatured(Boolean featured) { this.featured = featured; }
    }

    /**
     * Product statistics DTO.
     */
    public static class ProductStatistics {
        private Long totalProducts;
        private Long inStockProducts;
        private Long outOfStockProducts;
        private Long lowStockProducts;
        private BigDecimal averagePrice;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private List<Object[]> categoryDistribution;
        private List<Object[]> monthlyCreations;

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Long totalProducts;
            private Long inStockProducts;
            private Long outOfStockProducts;
            private Long lowStockProducts;
            private BigDecimal averagePrice;
            private BigDecimal minPrice;
            private BigDecimal maxPrice;
            private List<Object[]> categoryDistribution;
            private List<Object[]> monthlyCreations;

            public Builder totalProducts(Long totalProducts) {
                this.totalProducts = totalProducts;
                return this;
            }

            public Builder inStockProducts(Long inStockProducts) {
                this.inStockProducts = inStockProducts;
                return this;
            }

            public Builder outOfStockProducts(Long outOfStockProducts) {
                this.outOfStockProducts = outOfStockProducts;
                return this;
            }

            public Builder lowStockProducts(Long lowStockProducts) {
                this.lowStockProducts = lowStockProducts;
                return this;
            }

            public Builder averagePrice(BigDecimal averagePrice) {
                this.averagePrice = averagePrice;
                return this;
            }

            public Builder minPrice(BigDecimal minPrice) {
                this.minPrice = minPrice;
                return this;
            }

            public Builder maxPrice(BigDecimal maxPrice) {
                this.maxPrice = maxPrice;
                return this;
            }

            public Builder categoryDistribution(List<Object[]> categoryDistribution) {
                this.categoryDistribution = categoryDistribution;
                return this;
            }

            public Builder monthlyCreations(List<Object[]> monthlyCreations) {
                this.monthlyCreations = monthlyCreations;
                return this;
            }

            public ProductStatistics build() {
                ProductStatistics stats = new ProductStatistics();
                stats.totalProducts = this.totalProducts;
                stats.inStockProducts = this.inStockProducts;
                stats.outOfStockProducts = this.outOfStockProducts;
                stats.lowStockProducts = this.lowStockProducts;
                stats.averagePrice = this.averagePrice;
                stats.minPrice = this.minPrice;
                stats.maxPrice = this.maxPrice;
                stats.categoryDistribution = this.categoryDistribution;
                stats.monthlyCreations = this.monthlyCreations;
                return stats;
            }
        }

        // Getters and setters
        public Long getTotalProducts() { return totalProducts; }
        public void setTotalProducts(Long totalProducts) { this.totalProducts = totalProducts; }
        
        public Long getInStockProducts() { return inStockProducts; }
        public void setInStockProducts(Long inStockProducts) { this.inStockProducts = inStockProducts; }
        
        public Long getOutOfStockProducts() { return outOfStockProducts; }
        public void setOutOfStockProducts(Long outOfStockProducts) { this.outOfStockProducts = outOfStockProducts; }
        
        public Long getLowStockProducts() { return lowStockProducts; }
        public void setLowStockProducts(Long lowStockProducts) { this.lowStockProducts = lowStockProducts; }
        
        public BigDecimal getAveragePrice() { return averagePrice; }
        public void setAveragePrice(BigDecimal averagePrice) { this.averagePrice = averagePrice; }
        
        public BigDecimal getMinPrice() { return minPrice; }
        public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }
        
        public BigDecimal getMaxPrice() { return maxPrice; }
        public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }
        
        public List<Object[]> getCategoryDistribution() { return categoryDistribution; }
        public void setCategoryDistribution(List<Object[]> categoryDistribution) { this.categoryDistribution = categoryDistribution; }
        
        public List<Object[]> getMonthlyCreations() { return monthlyCreations; }
        public void setMonthlyCreations(List<Object[]> monthlyCreations) { this.monthlyCreations = monthlyCreations; }
    }
}
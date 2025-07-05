package com.acanbiler.shopwave.repository;

import com.acanbiler.shopwave.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Product entity operations.
 * 
 * This repository provides basic CRUD operations and custom query methods
 * for product management, search, and analytics.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Basic finder methods
    Optional<Product> findBySku(String sku);
    
    boolean existsBySku(String sku);
    
    List<Product> findByEnabled(boolean enabled);
    
    Page<Product> findByEnabled(boolean enabled, Pageable pageable);

    // Category-based queries
    List<Product> findByCategory(Product.ProductCategory category);
    
    Page<Product> findByCategory(Product.ProductCategory category, Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category = :category AND p.enabled = true")
    long countByCategory(@Param("category") Product.ProductCategory category);

    // Featured products
    List<Product> findByFeaturedAndEnabled(boolean featured, boolean enabled);
    
    Page<Product> findByFeaturedAndEnabled(boolean featured, boolean enabled, Pageable pageable);

    // Price-based queries
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    Page<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.price >= :minPrice AND p.price <= :maxPrice AND p.enabled = true")
    Page<Product> findByPriceRangeAndEnabled(@Param("minPrice") BigDecimal minPrice, 
                                           @Param("maxPrice") BigDecimal maxPrice, 
                                           Pageable pageable);

    // Stock-based queries
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= :threshold AND p.enabled = true")
    List<Product> findLowStockProducts(@Param("threshold") int threshold);
    
    @Query("SELECT p FROM Product p WHERE p.stockQuantity = 0 AND p.enabled = true")
    List<Product> findOutOfStockProducts();
    
    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0 AND p.enabled = true")
    Page<Product> findInStockProducts(Pageable pageable);

    // Brand-based queries
    List<Product> findByBrand(String brand);
    
    Page<Product> findByBrand(String brand, Pageable pageable);
    
    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.brand IS NOT NULL AND p.enabled = true ORDER BY p.brand")
    List<String> findAllBrands();

    // Search queries
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%')) AND " +
           "p.enabled = true")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE " +
           "p.category = :category AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "p.enabled = true")
    Page<Product> searchProductsByCategory(@Param("keyword") String keyword, 
                                         @Param("category") Product.ProductCategory category, 
                                         Pageable pageable);

    // Complex filtering query
    @Query("SELECT p FROM Product p WHERE " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:brand IS NULL OR LOWER(p.brand) = LOWER(:brand)) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:inStockOnly = false OR p.stockQuantity > 0) AND " +
           "p.enabled = true")
    Page<Product> findProductsWithFilters(@Param("category") Product.ProductCategory category,
                                        @Param("brand") String brand,
                                        @Param("minPrice") BigDecimal minPrice,
                                        @Param("maxPrice") BigDecimal maxPrice,
                                        @Param("inStockOnly") boolean inStockOnly,
                                        Pageable pageable);

    // Review-based queries
    @Query("SELECT p FROM Product p WHERE " +
           "(SELECT AVG(r.rating) FROM Review r WHERE r.product = p) >= :minRating AND " +
           "p.enabled = true")
    Page<Product> findProductsWithMinRating(@Param("minRating") double minRating, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE " +
           "(SELECT COUNT(r) FROM Review r WHERE r.product = p) >= :minReviews AND " +
           "p.enabled = true")
    Page<Product> findProductsWithMinReviews(@Param("minReviews") long minReviews, Pageable pageable);

    // Time-based queries
    List<Product> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT p FROM Product p WHERE p.createdAt >= :date AND p.enabled = true ORDER BY p.createdAt DESC")
    Page<Product> findNewProducts(@Param("date") LocalDateTime date, Pageable pageable);

    // Statistics queries
    @Query("SELECT p.category, COUNT(p) FROM Product p WHERE p.enabled = true GROUP BY p.category")
    List<Object[]> getProductCountByCategory();
    
    @Query("SELECT " +
           "COUNT(p) as totalProducts, " +
           "COUNT(CASE WHEN p.stockQuantity > 0 THEN 1 END) as inStockProducts, " +
           "COUNT(CASE WHEN p.stockQuantity = 0 THEN 1 END) as outOfStockProducts, " +
           "COUNT(CASE WHEN p.stockQuantity <= 10 AND p.stockQuantity > 0 THEN 1 END) as lowStockProducts, " +
           "AVG(p.price) as averagePrice, " +
           "MIN(p.price) as minPrice, " +
           "MAX(p.price) as maxPrice " +
           "FROM Product p WHERE p.enabled = true")
    Object getProductStatistics();
    
    @Query("SELECT " +
           "EXTRACT(YEAR FROM p.createdAt) as year, " +
           "EXTRACT(MONTH FROM p.createdAt) as month, " +
           "COUNT(p) as count " +
           "FROM Product p " +
           "WHERE p.createdAt >= :startDate AND p.enabled = true " +
           "GROUP BY EXTRACT(YEAR FROM p.createdAt), EXTRACT(MONTH FROM p.createdAt) " +
           "ORDER BY year, month")
    List<Object[]> getMonthlyProductCreationStats(@Param("startDate") LocalDateTime startDate);

    // Top products queries
    @Query("SELECT p, AVG(r.rating) as avgRating FROM Product p " +
           "LEFT JOIN p.reviews r " +
           "WHERE p.enabled = true " +
           "GROUP BY p " +
           "HAVING COUNT(r) >= :minReviews " +
           "ORDER BY avgRating DESC")
    Page<Object[]> findTopRatedProducts(@Param("minReviews") long minReviews, Pageable pageable);
    
    @Query("SELECT p, COUNT(r) as reviewCount FROM Product p " +
           "LEFT JOIN p.reviews r " +
           "WHERE p.enabled = true " +
           "GROUP BY p " +
           "ORDER BY reviewCount DESC")
    Page<Object[]> findMostReviewedProducts(Pageable pageable);

    // Update operations
    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :quantity WHERE p.id = :id AND p.stockQuantity >= :quantity")
    int decreaseStock(@Param("id") Long id, @Param("quantity") int quantity);
    
    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity + :quantity WHERE p.id = :id")
    int increaseStock(@Param("id") Long id, @Param("quantity") int quantity);
    
    @Modifying
    @Query("UPDATE Product p SET p.enabled = :enabled WHERE p.id = :id")
    int updateProductEnabledStatus(@Param("id") Long id, @Param("enabled") boolean enabled);
    
    @Modifying
    @Query("UPDATE Product p SET p.featured = :featured WHERE p.id = :id")
    int updateProductFeaturedStatus(@Param("id") Long id, @Param("featured") boolean featured);
    
    @Modifying
    @Query("UPDATE Product p SET p.price = :price WHERE p.id = :id")
    int updateProductPrice(@Param("id") Long id, @Param("price") BigDecimal price);

    // Category statistics
    @Query("SELECT p.category, COUNT(p), AVG(p.price), SUM(p.stockQuantity) FROM Product p " +
           "WHERE p.enabled = true " +
           "GROUP BY p.category " +
           "ORDER BY COUNT(p) DESC")
    List<Object[]> getCategoryStatistics();
    
    // Recent products
    @Query("SELECT p FROM Product p WHERE p.enabled = true ORDER BY p.createdAt DESC")
    Page<Product> findRecentProducts(Pageable pageable);
    
    // Popular products (based on review count)
    @Query("SELECT p FROM Product p " +
           "WHERE p.enabled = true AND " +
           "(SELECT COUNT(r) FROM Review r WHERE r.product = p) > 0 " +
           "ORDER BY (SELECT COUNT(r) FROM Review r WHERE r.product = p) DESC")
    Page<Product> findPopularProducts(Pageable pageable);
}
package com.acanbiler.shopwave.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Product entity representing products in the ShopWave application.
 * 
 * This entity includes product information, stock management,
 * and relationship with reviews for rating calculations.
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_category", columnList = "category"),
    @Index(name = "idx_product_enabled", columnList = "enabled"),
    @Index(name = "idx_product_name", columnList = "name"),
    @Index(name = "idx_product_price", columnList = "price")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"reviews"})
@ToString(exclude = {"reviews"})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(max = 100, message = "Product name must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Product description is required")
    @Size(max = 1000, message = "Product description must not exceed 1000 characters")
    @Column(nullable = false, length = 1000)
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @NotNull(message = "Category is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductCategory category;

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Size(max = 50, message = "SKU must not exceed 50 characters")
    @Column(length = 50, unique = true)
    private String sku;

    @Size(max = 50, message = "Brand must not exceed 50 characters")
    @Column(length = 50)
    private String brand;

    @DecimalMin(value = "0.0", message = "Weight cannot be negative")
    @Digits(integer = 5, fraction = 2, message = "Weight must have at most 5 integer digits and 2 decimal places")
    @Column(precision = 7, scale = 2)
    private BigDecimal weight;

    @Size(max = 50, message = "Dimensions must not exceed 50 characters")
    @Column(length = 50)
    private String dimensions;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "featured")
    @Builder.Default
    private Boolean featured = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Calculated fields
    @Formula("(SELECT AVG(r.rating) FROM reviews r WHERE r.product_id = id)")
    private Double averageRating;

    @Formula("(SELECT COUNT(r.id) FROM reviews r WHERE r.product_id = id)")
    private Long reviewCount;

    // Relationships
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    // Utility methods
    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }

    public boolean isLowStock() {
        return stockQuantity != null && stockQuantity > 0 && stockQuantity <= 10;
    }

    public boolean isOutOfStock() {
        return stockQuantity == null || stockQuantity <= 0;
    }

    public void decreaseStock(int quantity) {
        if (stockQuantity == null || stockQuantity < quantity) {
            throw new IllegalArgumentException("Insufficient stock available");
        }
        this.stockQuantity -= quantity;
    }

    public void increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.stockQuantity = (this.stockQuantity == null ? 0 : this.stockQuantity) + quantity;
    }

    public String getFormattedPrice() {
        return price != null ? String.format("$%.2f", price) : "$0.00";
    }

    public Double getRatingOrDefault() {
        return averageRating != null ? averageRating : 0.0;
    }

    public Long getReviewCountOrDefault() {
        return reviewCount != null ? reviewCount : 0L;
    }

    /**
     * Product category enumeration.
     */
    public enum ProductCategory {
        ELECTRONICS("Electronics"),
        CLOTHING("Clothing"),
        HOME_APPLIANCES("Home Appliances"),
        BOOKS("Books"),
        SPORTS("Sports & Outdoors"),
        BEAUTY("Beauty & Personal Care"),
        AUTOMOTIVE("Automotive"),
        TOYS("Toys & Games"),
        HEALTH("Health & Household"),
        JEWELRY("Jewelry"),
        MUSIC("Music"),
        VIDEO_GAMES("Video Games"),
        OFFICE("Office Products"),
        GARDEN("Garden & Outdoor"),
        FOOD("Food & Beverages");

        private final String displayName;

        ProductCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
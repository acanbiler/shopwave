package com.acanbiler.shopwave.controller;

import com.acanbiler.shopwave.entity.Product;
import com.acanbiler.shopwave.entity.Review;
import com.acanbiler.shopwave.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Product management controller for catalog operations.
 * 
 * This controller provides endpoints for product CRUD operations, search,
 * filtering, inventory management, and review handling.
 */
@RestController
@RequestMapping("/api/products")
@Tag(name = "Product Management", description = "Product catalog and inventory management operations")
@Validated
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Get all products with pagination.
     */
    @Operation(
        summary = "Get all products",
        description = "Retrieves paginated list of all enabled products with optional filtering."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Products retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductPageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<ProductPageResponse> getAllProducts(
        @Parameter(description = "Product category filter", example = "ELECTRONICS")
        @RequestParam(required = false) Product.ProductCategory category,
        @Parameter(description = "Brand filter", example = "Apple")
        @RequestParam(required = false) String brand,
        @Parameter(description = "Minimum price filter", example = "10.00")
        @RequestParam(required = false) BigDecimal minPrice,
        @Parameter(description = "Maximum price filter", example = "1000.00")
        @RequestParam(required = false) BigDecimal maxPrice,
        @Parameter(description = "Show only in-stock products", example = "true")
        @RequestParam(defaultValue = "false") boolean inStockOnly,
        @Parameter(description = "Pagination parameters")
        @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        Page<Product> products;
        
        // Use filtered search if any filters are provided
        if (category != null || brand != null || minPrice != null || maxPrice != null || inStockOnly) {
            products = productService.findWithFilters(category, brand, minPrice, maxPrice, inStockOnly, pageable);
        } else {
            products = productService.findAll(pageable);
        }
        
        ProductPageResponse response = ProductPageResponse.fromPage(products);
        return ResponseEntity.ok(response);
    }

    /**
     * Search products by keyword.
     */
    @Operation(
        summary = "Search products",
        description = "Searches products by name, description, or brand using keyword matching."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductPageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/search")
    public ResponseEntity<ProductPageResponse> searchProducts(
        @Parameter(description = "Search keyword", example = "smartphone", required = true)
        @RequestParam String keyword,
        @Parameter(description = "Pagination parameters")
        @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        
        Page<Product> products = productService.searchProducts(keyword, pageable);
        ProductPageResponse response = ProductPageResponse.fromPage(products);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get products by category.
     */
    @Operation(
        summary = "Get products by category",
        description = "Retrieves products filtered by specific category."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Products retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductPageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid category"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/category/{category}")
    public ResponseEntity<ProductPageResponse> getProductsByCategory(
        @Parameter(description = "Product category", example = "ELECTRONICS", required = true)
        @PathVariable Product.ProductCategory category,
        @Parameter(description = "Pagination parameters")
        @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        
        Page<Product> products = productService.findByCategory(category, pageable);
        ProductPageResponse response = ProductPageResponse.fromPage(products);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get featured products.
     */
    @Operation(
        summary = "Get featured products",
        description = "Retrieves products marked as featured for homepage display."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Featured products retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductPageResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/featured")
    public ResponseEntity<ProductPageResponse> getFeaturedProducts(
        @Parameter(description = "Pagination parameters")
        @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        
        Page<Product> products = productService.findFeaturedProducts(pageable);
        ProductPageResponse response = ProductPageResponse.fromPage(products);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get new products.
     */
    @Operation(
        summary = "Get new products",
        description = "Retrieves recently added products within specified number of days."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "New products retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductPageResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/new")
    public ResponseEntity<ProductPageResponse> getNewProducts(
        @Parameter(description = "Number of days to consider as new", example = "7")
        @RequestParam(defaultValue = "7") int days,
        @Parameter(description = "Pagination parameters")
        @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        Page<Product> products = productService.findNewProducts(days, pageable);
        ProductPageResponse response = ProductPageResponse.fromPage(products);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get popular products.
     */
    @Operation(
        summary = "Get popular products",
        description = "Retrieves products sorted by popularity based on reviews and ratings."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Popular products retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductPageResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/popular")
    public ResponseEntity<ProductPageResponse> getPopularProducts(
        @Parameter(description = "Pagination parameters")
        @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        
        Page<Product> products = productService.findPopularProducts(pageable);
        ProductPageResponse response = ProductPageResponse.fromPage(products);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get product by ID.
     */
    @Operation(
        summary = "Get product by ID",
        description = "Retrieves detailed product information by product ID."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
        @Parameter(description = "Product ID", example = "123", required = true)
        @PathVariable Long id) {
        
        Product product = productService.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
            
        ProductResponse response = ProductResponse.fromProduct(product);
        return ResponseEntity.ok(response);
    }

    /**
     * Get product by SKU.
     */
    @Operation(
        summary = "Get product by SKU",
        description = "Retrieves product information by SKU (Stock Keeping Unit)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductResponse> getProductBySku(
        @Parameter(description = "Product SKU", example = "PHONE-123", required = true)
        @PathVariable String sku) {
        
        Product product = productService.findBySku(sku)
            .orElseThrow(() -> new RuntimeException("Product not found"));
            
        ProductResponse response = ProductResponse.fromProduct(product);
        return ResponseEntity.ok(response);
    }

    /**
     * Create new product (admin only).
     */
    @Operation(
        summary = "Create new product",
        description = "Creates a new product in the catalog. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Product created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation failed or SKU already exists"),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ProductResponse> createProduct(
        @Parameter(description = "Product creation details", required = true)
        @Valid @RequestBody ProductCreateRequest request) {
        
        Product product = productService.createProduct(request.toServiceRequest());
        ProductResponse response = ProductResponse.fromProduct(product);
        
        return ResponseEntity.status(201).body(response);
    }

    /**
     * Update product (admin only).
     */
    @Operation(
        summary = "Update product",
        description = "Updates existing product information. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation failed"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ProductResponse> updateProduct(
        @Parameter(description = "Product ID", example = "123", required = true)
        @PathVariable Long id,
        @Parameter(description = "Product update details", required = true)
        @Valid @RequestBody ProductUpdateRequest request) {
        
        Product product = productService.updateProduct(id, request.toServiceRequest());
        ProductResponse response = ProductResponse.fromProduct(product);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Delete product (admin only).
     */
    @Operation(
        summary = "Delete product",
        description = "Permanently deletes a product from the catalog. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Void> deleteProduct(
        @Parameter(description = "Product ID", example = "123", required = true)
        @PathVariable Long id) {
        
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Update product stock (admin only).
     */
    @Operation(
        summary = "Update product stock",
        description = "Updates product stock quantity. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Stock updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ProductResponse> updateStock(
        @Parameter(description = "Product ID", example = "123", required = true)
        @PathVariable Long id,
        @Parameter(description = "Quantity change (positive to add, negative to remove)", example = "10", required = true)
        @RequestParam int quantity) {
        
        Product product = productService.updateStock(id, quantity);
        ProductResponse response = ProductResponse.fromProduct(product);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Set product featured status (admin only).
     */
    @Operation(
        summary = "Set product featured status",
        description = "Marks or unmarks product as featured. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Featured status updated successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{id}/featured")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<MessageResponse> setFeatured(
        @Parameter(description = "Product ID", example = "123", required = true)
        @PathVariable Long id,
        @Parameter(description = "Featured status", example = "true", required = true)
        @RequestParam boolean featured) {
        
        productService.setFeatured(id, featured);
        
        MessageResponse response = MessageResponse.builder()
            .message("Product " + (featured ? "marked as featured" : "unmarked as featured"))
            .timestamp(LocalDateTime.now())
            .build();
            
        return ResponseEntity.ok(response);
    }

    /**
     * Get product reviews.
     */
    @Operation(
        summary = "Get product reviews",
        description = "Retrieves paginated list of approved reviews for a specific product."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}/reviews")
    public ResponseEntity<ReviewPageResponse> getProductReviews(
        @Parameter(description = "Product ID", example = "123", required = true)
        @PathVariable Long id,
        @Parameter(description = "Pagination parameters")
        @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        
        Page<Review> reviews = productService.getProductReviews(id, pageable);
        ReviewPageResponse response = ReviewPageResponse.fromPage(reviews);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get all product brands.
     */
    @Operation(
        summary = "Get all brands",
        description = "Retrieves list of all unique product brands in the catalog."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Brands retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/brands")
    public ResponseEntity<List<String>> getAllBrands() {
        
        List<String> brands = productService.getAllBrands();
        return ResponseEntity.ok(brands);
    }

    /**
     * Get low stock products (admin only).
     */
    @Operation(
        summary = "Get low stock products",
        description = "Retrieves products with stock below specified threshold. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Low stock products retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<List<ProductResponse>> getLowStockProducts(
        @Parameter(description = "Stock threshold", example = "10")
        @RequestParam(defaultValue = "10") int threshold) {
        
        List<Product> products = productService.getLowStockProducts(threshold);
        List<ProductResponse> response = products.stream()
            .map(ProductResponse::fromProduct)
            .toList();
            
        return ResponseEntity.ok(response);
    }

    /**
     * Get product statistics (admin only).
     */
    @Operation(
        summary = "Get product statistics",
        description = "Retrieves comprehensive product statistics and analytics. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductService.ProductStatistics.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ProductService.ProductStatistics> getProductStatistics() {
        
        ProductService.ProductStatistics statistics = productService.getProductStatistics();
        return ResponseEntity.ok(statistics);
    }

    // DTOs for API requests and responses

    /**
     * Product creation request DTO.
     */
    @Schema(description = "Product creation request containing all required product information")
    public static class ProductCreateRequest {
        
        @Schema(description = "Product name", example = "iPhone 15 Pro", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Product name is required")
        @Size(max = 200, message = "Product name must not exceed 200 characters")
        private String name;

        @Schema(description = "Product description", example = "Latest iPhone with advanced features")
        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        private String description;

        @Schema(description = "Product price", example = "999.99", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Price is required")
        @Positive(message = "Price must be positive")
        private BigDecimal price;

        @Schema(description = "Stock quantity", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Stock quantity is required")
        private Integer stockQuantity;

        @Schema(description = "Product category", example = "ELECTRONICS", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Category is required")
        private Product.ProductCategory category;

        @Schema(description = "Product image URL", example = "https://example.com/iphone.jpg")
        private String imageUrl;

        @Schema(description = "Product SKU", example = "IPHONE-15-PRO-128")
        @Size(max = 50, message = "SKU must not exceed 50 characters")
        private String sku;

        @Schema(description = "Product brand", example = "Apple")
        @Size(max = 100, message = "Brand must not exceed 100 characters")
        private String brand;

        @Schema(description = "Product weight in kg", example = "0.187")
        private BigDecimal weight;

        @Schema(description = "Product dimensions", example = "146.6 x 70.6 x 7.65 mm")
        @Size(max = 100, message = "Dimensions must not exceed 100 characters")
        private String dimensions;

        @Schema(description = "Product enabled status", example = "true")
        private Boolean enabled;

        @Schema(description = "Product featured status", example = "false")
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

        public ProductService.ProductCreateRequest toServiceRequest() {
            ProductService.ProductCreateRequest request = new ProductService.ProductCreateRequest();
            request.setName(this.name);
            request.setDescription(this.description);
            request.setPrice(this.price);
            request.setStockQuantity(this.stockQuantity);
            request.setCategory(this.category);
            request.setImageUrl(this.imageUrl);
            request.setSku(this.sku);
            request.setBrand(this.brand);
            request.setWeight(this.weight);
            request.setDimensions(this.dimensions);
            request.setEnabled(this.enabled);
            request.setFeatured(this.featured);
            return request;
        }
    }

    /**
     * Product update request DTO.
     */
    @Schema(description = "Product update request for modifying product information")
    public static class ProductUpdateRequest {
        
        @Schema(description = "Product name", example = "iPhone 15 Pro Max")
        @Size(max = 200, message = "Product name must not exceed 200 characters")
        private String name;

        @Schema(description = "Product description", example = "Updated description")
        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        private String description;

        @Schema(description = "Product price", example = "1099.99")
        @Positive(message = "Price must be positive")
        private BigDecimal price;

        @Schema(description = "Stock quantity", example = "150")
        private Integer stockQuantity;

        @Schema(description = "Product category", example = "ELECTRONICS")
        private Product.ProductCategory category;

        @Schema(description = "Product image URL", example = "https://example.com/iphone-new.jpg")
        private String imageUrl;

        @Schema(description = "Product SKU", example = "IPHONE-15-PRO-MAX-256")
        @Size(max = 50, message = "SKU must not exceed 50 characters")
        private String sku;

        @Schema(description = "Product brand", example = "Apple")
        @Size(max = 100, message = "Brand must not exceed 100 characters")
        private String brand;

        @Schema(description = "Product weight in kg", example = "0.221")
        private BigDecimal weight;

        @Schema(description = "Product dimensions", example = "159.9 x 76.7 x 7.65 mm")
        @Size(max = 100, message = "Dimensions must not exceed 100 characters")
        private String dimensions;

        @Schema(description = "Product enabled status", example = "true")
        private Boolean enabled;

        @Schema(description = "Product featured status", example = "true")
        private Boolean featured;

        // Constructors
        public ProductUpdateRequest() {}

        // Getters and setters (same pattern as create request)
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

        public ProductService.ProductUpdateRequest toServiceRequest() {
            ProductService.ProductUpdateRequest request = new ProductService.ProductUpdateRequest();
            request.setName(this.name);
            request.setDescription(this.description);
            request.setPrice(this.price);
            request.setStockQuantity(this.stockQuantity);
            request.setCategory(this.category);
            request.setImageUrl(this.imageUrl);
            request.setSku(this.sku);
            request.setBrand(this.brand);
            request.setWeight(this.weight);
            request.setDimensions(this.dimensions);
            request.setEnabled(this.enabled);
            request.setFeatured(this.featured);
            return request;
        }
    }

    /**
     * Product response DTO.
     */
    @Schema(description = "Product information response")
    public static class ProductResponse {
        
        @Schema(description = "Product unique identifier", example = "123")
        private Long id;

        @Schema(description = "Product name", example = "iPhone 15 Pro")
        private String name;

        @Schema(description = "Product description", example = "Latest iPhone with advanced features")
        private String description;

        @Schema(description = "Product price", example = "999.99")
        private BigDecimal price;

        @Schema(description = "Stock quantity", example = "100")
        private Integer stockQuantity;

        @Schema(description = "Product category", example = "ELECTRONICS")
        private String category;

        @Schema(description = "Product image URL", example = "https://example.com/iphone.jpg")
        private String imageUrl;

        @Schema(description = "Product SKU", example = "IPHONE-15-PRO-128")
        private String sku;

        @Schema(description = "Product brand", example = "Apple")
        private String brand;

        @Schema(description = "Product weight in kg", example = "0.187")
        private BigDecimal weight;

        @Schema(description = "Product dimensions", example = "146.6 x 70.6 x 7.65 mm")
        private String dimensions;

        @Schema(description = "Product enabled status", example = "true")
        private Boolean enabled;

        @Schema(description = "Product featured status", example = "false")
        private Boolean featured;

        @Schema(description = "Average rating", example = "4.5")
        private BigDecimal averageRating;

        @Schema(description = "Total review count", example = "256")
        private Long reviewCount;

        @Schema(description = "In stock status", example = "true")
        private Boolean inStock;

        @Schema(description = "Creation timestamp", example = "2024-01-01T00:00:00")
        private LocalDateTime createdAt;

        @Schema(description = "Last update timestamp", example = "2024-01-15T10:30:00")
        private LocalDateTime updatedAt;

        public static ProductResponse fromProduct(Product product) {
            ProductResponse response = new ProductResponse();
            response.id = product.getId();
            response.name = product.getName();
            response.description = product.getDescription();
            response.price = product.getPrice();
            response.stockQuantity = product.getStockQuantity();
            response.category = product.getCategory().name();
            response.imageUrl = product.getImageUrl();
            response.sku = product.getSku();
            response.brand = product.getBrand();
            response.weight = product.getWeight();
            response.dimensions = product.getDimensions();
            response.enabled = product.getEnabled();
            response.featured = product.getFeatured();
            response.averageRating = product.getAverageRating() != null ? 
                BigDecimal.valueOf(product.getAverageRating()) : BigDecimal.ZERO;
            response.reviewCount = product.getReviewCount();
            response.inStock = product.isInStock();
            response.createdAt = product.getCreatedAt();
            response.updatedAt = product.getUpdatedAt();
            return response;
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }

        public Integer getStockQuantity() { return stockQuantity; }
        public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

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

        public BigDecimal getAverageRating() { return averageRating; }
        public void setAverageRating(BigDecimal averageRating) { this.averageRating = averageRating; }

        public Long getReviewCount() { return reviewCount; }
        public void setReviewCount(Long reviewCount) { this.reviewCount = reviewCount; }

        public Boolean getInStock() { return inStock; }
        public void setInStock(Boolean inStock) { this.inStock = inStock; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }

    /**
     * Paginated product response DTO.
     */
    @Schema(description = "Paginated product response")
    public static class ProductPageResponse {
        
        @Schema(description = "List of products")
        private List<ProductResponse> products;

        @Schema(description = "Current page number", example = "0")
        private int page;

        @Schema(description = "Page size", example = "20")
        private int size;

        @Schema(description = "Total number of elements", example = "500")
        private long totalElements;

        @Schema(description = "Total number of pages", example = "25")
        private int totalPages;

        @Schema(description = "Is first page", example = "true")
        private boolean first;

        @Schema(description = "Is last page", example = "false")
        private boolean last;

        public static ProductPageResponse fromPage(Page<Product> page) {
            ProductPageResponse response = new ProductPageResponse();
            response.products = page.getContent().stream()
                .map(ProductResponse::fromProduct)
                .toList();
            response.page = page.getNumber();
            response.size = page.getSize();
            response.totalElements = page.getTotalElements();
            response.totalPages = page.getTotalPages();
            response.first = page.isFirst();
            response.last = page.isLast();
            return response;
        }

        // Getters and setters
        public List<ProductResponse> getProducts() { return products; }
        public void setProducts(List<ProductResponse> products) { this.products = products; }

        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }

        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }

        public long getTotalElements() { return totalElements; }
        public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

        public boolean isFirst() { return first; }
        public void setFirst(boolean first) { this.first = first; }

        public boolean isLast() { return last; }
        public void setLast(boolean last) { this.last = last; }
    }

    /**
     * Review response DTO.
     */
    @Schema(description = "Product review response")
    public static class ReviewResponse {
        
        @Schema(description = "Review unique identifier", example = "456")
        private Long id;

        @Schema(description = "Review rating (1-5)", example = "5")
        private Integer rating;

        @Schema(description = "Review comment", example = "Excellent product!")
        private String comment;

        @Schema(description = "Reviewer name", example = "John Doe")
        private String reviewerName;

        @Schema(description = "Review creation timestamp", example = "2024-01-15T10:30:00")
        private LocalDateTime createdAt;

        public static ReviewResponse fromReview(Review review) {
            ReviewResponse response = new ReviewResponse();
            response.id = review.getId();
            response.rating = review.getRating();
            response.comment = review.getComment();
            response.reviewerName = review.getUser().getFullName();
            response.createdAt = review.getCreatedAt();
            return response;
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }

        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }

        public String getReviewerName() { return reviewerName; }
        public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    /**
     * Paginated review response DTO.
     */
    @Schema(description = "Paginated review response")
    public static class ReviewPageResponse {
        
        @Schema(description = "List of reviews")
        private List<ReviewResponse> reviews;

        @Schema(description = "Current page number", example = "0")
        private int page;

        @Schema(description = "Page size", example = "10")
        private int size;

        @Schema(description = "Total number of elements", example = "100")
        private long totalElements;

        @Schema(description = "Total number of pages", example = "10")
        private int totalPages;

        @Schema(description = "Is first page", example = "true")
        private boolean first;

        @Schema(description = "Is last page", example = "false")
        private boolean last;

        public static ReviewPageResponse fromPage(Page<Review> page) {
            ReviewPageResponse response = new ReviewPageResponse();
            response.reviews = page.getContent().stream()
                .map(ReviewResponse::fromReview)
                .toList();
            response.page = page.getNumber();
            response.size = page.getSize();
            response.totalElements = page.getTotalElements();
            response.totalPages = page.getTotalPages();
            response.first = page.isFirst();
            response.last = page.isLast();
            return response;
        }

        // Getters and setters
        public List<ReviewResponse> getReviews() { return reviews; }
        public void setReviews(List<ReviewResponse> reviews) { this.reviews = reviews; }

        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }

        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }

        public long getTotalElements() { return totalElements; }
        public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

        public boolean isFirst() { return first; }
        public void setFirst(boolean first) { this.first = first; }

        public boolean isLast() { return last; }
        public void setLast(boolean last) { this.last = last; }
    }

    /**
     * Message response DTO.
     */
    @Schema(description = "Standard message response")
    public static class MessageResponse {
        
        @Schema(description = "Response message", example = "Operation completed successfully")
        private String message;

        @Schema(description = "Response timestamp", example = "2024-01-15T10:30:00")
        private LocalDateTime timestamp;

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String message;
            private LocalDateTime timestamp;

            public Builder message(String message) { this.message = message; return this; }
            public Builder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }

            public MessageResponse build() {
                MessageResponse response = new MessageResponse();
                response.message = this.message;
                response.timestamp = this.timestamp;
                return response;
            }
        }

        // Getters and setters
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
}
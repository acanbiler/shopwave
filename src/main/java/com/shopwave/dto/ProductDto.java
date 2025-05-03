package com.shopwave.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductDto extends BaseDto {
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private BigDecimal discount;
    private Boolean isActive;
    private Long categoryId;
    private String categoryName;
    private Double averageRating;
    private Long reviewCount;
} 
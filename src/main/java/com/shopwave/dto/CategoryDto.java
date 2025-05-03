package com.shopwave.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CategoryDto extends BaseDto {
    private String name;
    private String description;
    private String imageUrl;
    private Long parentId;
    private String parentName;
    private List<CategoryDto> subCategories;
    private Long productCount;
} 
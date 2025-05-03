package com.shopwave.mapper;

import com.shopwave.dto.ProductDto;
import com.shopwave.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProductMapper extends EntityMapper<Product, ProductDto> {
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    @Override
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    ProductDto toDto(Product product);

    @Override
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    Product toEntity(ProductDto productDto);
} 
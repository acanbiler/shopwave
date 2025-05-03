package com.shopwave.mapper;

import com.shopwave.dto.CategoryDto;
import com.shopwave.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface CategoryMapper extends EntityMapper<Category, CategoryDto> {
    CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

    @Override
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    @Mapping(target = "subCategories", ignore = true)
    @Mapping(target = "products", ignore = true)
    CategoryDto toDto(Category category);

    @Override
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "subCategories", ignore = true)
    @Mapping(target = "products", ignore = true)
    Category toEntity(CategoryDto categoryDto);

    List<CategoryDto> toDtoList(List<Category> categories);
    List<Category> toEntityList(List<CategoryDto> categoryDtos);
} 
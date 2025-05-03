package com.shopwave.mapper;

import com.shopwave.dto.ReviewDto;
import com.shopwave.model.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ReviewMapper extends EntityMapper<Review, ReviewDto> {
    ReviewMapper INSTANCE = Mappers.getMapper(ReviewMapper.class);

    @Override
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userFirstName", source = "user.firstName")
    @Mapping(target = "userLastName", source = "user.lastName")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    ReviewDto toDto(Review review);

    @Override
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "product", ignore = true)
    Review toEntity(ReviewDto reviewDto);
} 
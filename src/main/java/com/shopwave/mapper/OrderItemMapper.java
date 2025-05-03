package com.shopwave.mapper;

import com.shopwave.dto.OrderItemDto;
import com.shopwave.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface OrderItemMapper extends EntityMapper<OrderItem, OrderItemDto> {
    OrderItemMapper INSTANCE = Mappers.getMapper(OrderItemMapper.class);

    @Override
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productImageUrl", source = "product.imageUrl")
    OrderItemDto toDto(OrderItem orderItem);

    @Override
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product", ignore = true)
    OrderItem toEntity(OrderItemDto orderItemDto);
} 
package com.shopwave.mapper;

import com.shopwave.dto.OrderDto;
import com.shopwave.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(uses = {OrderItemMapper.class})
public interface OrderMapper extends EntityMapper<Order, OrderDto> {
    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    @Override
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "orderItems", source = "orderItems")
    OrderDto toDto(Order order);

    @Override
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    Order toEntity(OrderDto orderDto);

    List<OrderDto> toDtoList(List<Order> orders);
    List<Order> toEntityList(List<OrderDto> orderDtos);
} 
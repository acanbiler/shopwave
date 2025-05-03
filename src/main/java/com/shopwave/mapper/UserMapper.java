package com.shopwave.mapper;

import com.shopwave.dto.UserDto;
import com.shopwave.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper extends EntityMapper<User, UserDto> {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Override
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    UserDto toDto(User user);

    @Override
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    User toEntity(UserDto userDto);
} 
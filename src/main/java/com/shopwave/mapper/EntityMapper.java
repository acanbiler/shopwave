package com.shopwave.mapper;

import com.shopwave.dto.BaseDto;
import com.shopwave.model.BaseEntity;

public interface EntityMapper<E extends BaseEntity, D extends BaseDto> {
    D toDto(E entity);
    E toEntity(D dto);
} 
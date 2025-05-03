package com.shopwave.service;

import com.shopwave.dto.BaseDto;
import com.shopwave.model.BaseEntity;

import java.util.List;

public interface BaseService<E extends BaseEntity, D extends BaseDto> {
    D create(D dto);
    D getById(Long id);
    List<D> getAll();
    D update(Long id, D dto);
    void delete(Long id);
} 
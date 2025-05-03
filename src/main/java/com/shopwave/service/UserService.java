package com.shopwave.service;

import com.shopwave.dto.UserDto;
import com.shopwave.model.User;

import java.util.Optional;

public interface UserService extends BaseService<User, UserDto> {
    Optional<UserDto> findByFirebaseUid(String firebaseUid);
    Optional<UserDto> findByEmail(String email);
    boolean existsByEmail(String email);
    UserDto updateProfile(Long userId, UserDto userDto);
    void updateUserRole(Long userId, User.UserRole role);
} 
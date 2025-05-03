package com.shopwave.service.impl;

import com.shopwave.dto.UserDto;
import com.shopwave.exception.ResourceNotFoundException;
import com.shopwave.mapper.UserMapper;
import com.shopwave.model.User;
import com.shopwave.repository.UserRepository;
import com.shopwave.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        User user = userMapper.toEntity(userDto);
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public UserDto update(Long id, UserDto userDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        User updatedUser = userMapper.toEntity(userDto);
        updatedUser.setId(existingUser.getId());
        updatedUser.setCreatedAt(existingUser.getCreatedAt());
        updatedUser.setFirebaseUid(existingUser.getFirebaseUid());
        updatedUser.setRole(existingUser.getRole());

        User savedUser = userRepository.save(updatedUser);
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> findByFirebaseUid(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid)
                .map(userMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public UserDto updateProfile(Long userId, UserDto userDto) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Only update profile-related fields
        existingUser.setFirstName(userDto.getFirstName());
        existingUser.setLastName(userDto.getLastName());
        existingUser.setPhoneNumber(userDto.getPhoneNumber());
        existingUser.setAddress(userDto.getAddress());

        User savedUser = userRepository.save(existingUser);
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public void updateUserRole(Long userId, User.UserRole role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        user.setRole(role);
        userRepository.save(user);
    }
} 
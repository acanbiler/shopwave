package com.shopwave.service;

import com.shopwave.dto.UserDto;
import com.shopwave.exception.ResourceNotFoundException;
import com.shopwave.model.User;
import com.shopwave.repository.UserRepository;
import com.shopwave.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPhoneNumber("1234567890");

        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setEmail("test@example.com");
        userDto.setFirstName("John");
        userDto.setLastName("Doe");
        userDto.setPhoneNumber("1234567890");
    }

    @Test
    void createUser_ShouldReturnUserDto() {
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.create(userDto);

        assertNotNull(result);
        assertEquals(userDto.getEmail(), result.getEmail());
        assertEquals(userDto.getFirstName(), result.getFirstName());
        assertEquals(userDto.getLastName(), result.getLastName());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUserDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto result = userService.getById(1L);

        assertNotNull(result);
        assertEquals(userDto.getEmail(), result.getEmail());
        assertEquals(userDto.getFirstName(), result.getFirstName());
        assertEquals(userDto.getLastName(), result.getLastName());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getById(1L));
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void updateUser_WhenUserExists_ShouldReturnUpdatedUserDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto updatedDto = new UserDto();
        updatedDto.setId(1L);
        updatedDto.setEmail("updated@example.com");
        updatedDto.setFirstName("Updated");
        updatedDto.setLastName("Name");
        updatedDto.setPhoneNumber("9876543210");

        UserDto result = userService.update(1L, updatedDto);

        assertNotNull(result);
        assertEquals(updatedDto.getEmail(), result.getEmail());
        assertEquals(updatedDto.getFirstName(), result.getFirstName());
        assertEquals(updatedDto.getLastName(), result.getLastName());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void deleteUser_WhenUserExists_ShouldDeleteUser() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        userService.delete(1L);

        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_WhenUserDoesNotExist_ShouldThrowException() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> userService.delete(1L));
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, never()).deleteById(anyLong());
    }
} 
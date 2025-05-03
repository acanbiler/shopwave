package com.shopwave.controller;

import com.shopwave.dto.UserDto;
import com.shopwave.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setEmail("test@example.com");
        userDto.setFirstName("John");
        userDto.setLastName("Doe");
        userDto.setPhoneNumber("1234567890");
    }

    @Test
    void createUser_ShouldReturnCreatedUser() {
        when(userService.create(any(UserDto.class))).thenReturn(userDto);

        ResponseEntity<UserDto> response = userController.createUser(userDto);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(userDto, response.getBody());
        verify(userService, times(1)).create(any(UserDto.class));
    }

    @Test
    void getUserById_ShouldReturnUser() {
        when(userService.getById(1L)).thenReturn(userDto);

        ResponseEntity<UserDto> response = userController.getUserById(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userDto, response.getBody());
        verify(userService, times(1)).getById(1L);
    }

    @Test
    void getAllUsers_ShouldReturnUserList() {
        List<UserDto> userList = Arrays.asList(userDto);
        when(userService.getAll()).thenReturn(userList);

        ResponseEntity<List<UserDto>> response = userController.getAllUsers();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userList, response.getBody());
        verify(userService, times(1)).getAll();
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() {
        when(userService.update(anyLong(), any(UserDto.class))).thenReturn(userDto);

        ResponseEntity<UserDto> response = userController.updateUser(1L, userDto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userDto, response.getBody());
        verify(userService, times(1)).update(anyLong(), any(UserDto.class));
    }

    @Test
    void deleteUser_ShouldReturnNoContent() {
        doNothing().when(userService).delete(1L);

        ResponseEntity<Void> response = userController.deleteUser(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService, times(1)).delete(1L);
    }
} 
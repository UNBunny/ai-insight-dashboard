package com.example.demo.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.dto.UserDto;
import com.example.demo.dto.UserMapper;
import com.example.demo.models.User;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.impl.UserServiceImpl;

/**
 * Модульные тесты для класса UserServiceImpl с использованием Mockito
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    
    @Spy
    private UserMapper userMapper = Mappers.getMapper(UserMapper.class);
    
    @InjectMocks
    private UserServiceImpl userService;
    
    private User testUser;
    private UserDto testUserDto;
    
    @BeforeEach
    public void setup() {
        // Подготовка тестовых данных
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        
        testUserDto = new UserDto();
        testUserDto.setId(1L);
        testUserDto.setUsername("testuser");
        testUserDto.setEmail("test@example.com");
    }
    
    @Test
    public void testCreateUser() {
        // Подготовка
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // Выполнение
        UserDto createdUserDto = userService.createUser(testUserDto);
        
        // Проверка
        assertThat(createdUserDto).isNotNull();
        assertThat(createdUserDto.getId()).isEqualTo(1L);
        assertThat(createdUserDto.getUsername()).isEqualTo("testuser");
        assertThat(createdUserDto.getEmail()).isEqualTo("test@example.com");
        
        // Проверка вызовов mock-объектов
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).toEntity(any(UserDto.class));
        verify(userMapper, times(1)).toDto(any(User.class));
    }
    
    @Test
    public void testGetUserById_Found() {
        // Подготовка
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        
        // Выполнение
        Optional<UserDto> foundUserDto = userService.getUserById(1L);
        
        // Проверка
        assertThat(foundUserDto).isPresent();
        UserDto userDto = foundUserDto.get();
        assertThat(userDto.getId()).isEqualTo(1L);
        assertThat(userDto.getUsername()).isEqualTo("testuser");
        assertThat(userDto.getEmail()).isEqualTo("test@example.com");
        
        // Проверка вызовов mock-объектов
        verify(userRepository, times(1)).findById(1L);
        verify(userMapper, times(1)).toDto(testUser);
    }
    
    @Test
    public void testGetUserById_NotFound() {
        // Подготовка
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // Выполнение
        Optional<UserDto> result = userService.getUserById(1L);
        
        // Проверка
        assertThat(result).isEmpty();
        verify(userRepository, times(1)).findById(1L);
        
        // Проверка вызовов mock-объектов
        verify(userRepository, times(1)).findById(1L);
    }
    
    @Test
    public void testGetAllUsers() {
        // Подготовка
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, anotherUser));
        
        // Выполнение
        List<UserDto> userDtos = userService.getAllUsers();
        
        // Проверка
        assertThat(userDtos).isNotNull();
        assertThat(userDtos).hasSize(2);
        assertThat(userDtos.get(0).getId()).isEqualTo(1L);
        assertThat(userDtos.get(1).getId()).isEqualTo(2L);
        
        // Проверка вызовов mock-объектов
        verify(userRepository, times(1)).findAll();
        verify(userMapper, times(2)).toDto(any(User.class));
    }
    
    @Test
    public void testUpdateUser() {
        // Подготовка
        UserDto updateDto = new UserDto();
        updateDto.setUsername("updateduser");
        updateDto.setEmail("updated@example.com");
        
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("updateduser");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setPassword("password123");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        
        // Выполнение
        Optional<UserDto> updatedUserDto = userService.updateUser(1L, updateDto);
        
        // Проверка
        assertThat(updatedUserDto).isPresent();
        UserDto userDto = updatedUserDto.get();
        assertThat(userDto.getId()).isEqualTo(1L);
        assertThat(userDto.getUsername()).isEqualTo("updateduser");
        assertThat(userDto.getEmail()).isEqualTo("updated@example.com");
        
        // Проверка вызовов mock-объектов
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).updateEntityFromDto(eq(updateDto), any(User.class));
        verify(userMapper, times(1)).toDto(any(User.class));
    }
    
    @Test
    public void testDeleteUser() {
        // Подготовка
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);
        
        // Выполнение
        userService.deleteUser(1L);
        
        // Проверка вызовов mock-объектов
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }
    
    @Test
    public void testDeleteUser_NotFound() {
        // Подготовка
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(false);
        
        // Выполнение
        boolean result = userService.deleteUser(userId);
        
        // Проверка
        assertThat(result).isFalse();
        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, never()).deleteById(userId);
    }
}

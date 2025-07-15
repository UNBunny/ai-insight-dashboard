package com.example.demo.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.dto.UserDto;
import com.example.demo.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Тесты REST контроллера UserController с использованием MockMvc
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private UserService userService;
    
    private UserDto testUserDto;
    
    @BeforeEach
    public void setup() {
        testUserDto = new UserDto();
        testUserDto.setId(1L);
        testUserDto.setUsername("testuser");
        testUserDto.setEmail("test@example.com");
    }
    
    @Test
    public void testCreateUser() throws Exception {
        // Подготовка
        when(userService.createUser(any(UserDto.class))).thenReturn(testUserDto);
        
        // Выполнение и проверка
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }
    
    @Test
    public void testGetUserById() throws Exception {
        // Подготовка
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUserDto));
        
        // Выполнение и проверка
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }
    
    @Test
    public void testGetUserById_NotFound() throws Exception {
        // Подготовка
        when(userService.getUserById(99L)).thenReturn(Optional.empty());
        
        // Выполнение и проверка
        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    public void testGetAllUsers() throws Exception {
        // Подготовка
        UserDto user2 = new UserDto();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        
        List<UserDto> users = Arrays.asList(testUserDto, user2);
        
        when(userService.getAllUsers()).thenReturn(users);
        
        // Выполнение и проверка
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("testuser"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].username").value("user2"));
    }
    
    @Test
    public void testUpdateUser() throws Exception {
        // Подготовка
        UserDto updateDto = new UserDto();
        updateDto.setUsername("updateduser");
        updateDto.setEmail("updated@example.com");
        
        UserDto updatedUser = new UserDto();
        updatedUser.setId(1L);
        updatedUser.setUsername("updateduser");
        updatedUser.setEmail("updated@example.com");
        
        when(userService.updateUser(anyLong(), any(UserDto.class))).thenReturn(Optional.of(updatedUser));
        
        // Выполнение и проверка
        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("updateduser"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }
    
    @Test
    public void testDeleteUser() throws Exception {
        // Подготовка
        when(userService.deleteUser(1L)).thenReturn(true);
        
        // Выполнение и проверка
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }
    
    @Test
    public void testDeleteUser_NotFound() throws Exception {
        // Подготовка
        when(userService.deleteUser(99L)).thenReturn(false);
        
        // Выполнение и проверка
        mockMvc.perform(delete("/api/users/99"))
                .andExpect(status().isNotFound());
    }
}

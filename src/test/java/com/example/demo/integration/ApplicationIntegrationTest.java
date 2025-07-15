package com.example.demo.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.demo.dto.UserDto;
import com.example.demo.models.User;
import com.example.demo.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Интеграционные тесты для всего приложения с полным контекстом Spring
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ApplicationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserRepository userRepository;
    
    @BeforeEach
    public void setup() {
        // Очищаем базу данных перед каждым тестом
        userRepository.deleteAll();
    }
    
    @Test
    public void testCreateUserFlow() throws Exception {
        // Создаем DTO для запроса
        UserDto userDto = new UserDto();
        userDto.setUsername("integrationuser");
        userDto.setEmail("integration@example.com");
        
        // 1. Создаем пользователя через API
        MvcResult result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("integrationuser"))
                .andExpect(jsonPath("$.email").value("integration@example.com"))
                .andReturn();
        
        // Извлекаем ID созданного пользователя
        String responseContent = result.getResponse().getContentAsString();
        UserDto createdUser = objectMapper.readValue(responseContent, UserDto.class);
        Long userId = createdUser.getId();
        
        // 2. Получаем пользователя по ID
        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value("integrationuser"));
        
        // 3. Проверяем, что пользователь сохранился в базе данных
        User savedUser = userRepository.findById(userId).orElseThrow();
        assertThat(savedUser.getUsername()).isEqualTo("integrationuser");
        assertThat(savedUser.getEmail()).isEqualTo("integration@example.com");
        
        // 4. Обновляем пользователя
        UserDto updateDto = new UserDto();
        updateDto.setUsername("updateduser");
        updateDto.setEmail("updated@example.com");
        
        mockMvc.perform(put("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updateduser"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
        
        // 5. Проверяем обновление в базе данных
        User updatedUser = userRepository.findById(userId).orElseThrow();
        assertThat(updatedUser.getUsername()).isEqualTo("updateduser");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
        
        // 6. Удаляем пользователя
        mockMvc.perform(delete("/api/users/" + userId))
                .andExpect(status().isNoContent());
        
        // 7. Проверяем, что пользователь удален из базы данных
        assertThat(userRepository.findById(userId)).isEmpty();
    }
    
    @Test
    public void testGetAllUsers() throws Exception {
        // Подготовка - создаем пользователей напрямую в репозитории
        User user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setPassword("password1");
        
        User user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPassword("password2");
        
        userRepository.save(user1);
        userRepository.save(user2);
        
        // Выполнение и проверка
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").exists())
                .andExpect(jsonPath("$[1].username").exists());
    }
}

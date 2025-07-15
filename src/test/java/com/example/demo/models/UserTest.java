package com.example.demo.models;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Модульные тесты для класса User
 */
public class UserTest {
    
    @Test
    public void testUserCreation() {
        // Подготовка
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        
        // Проверка
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getPassword()).isEqualTo("password123");
    }
    
    @Test
    public void testUserEquality() {
        // Подготовка
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("testuser");
        user1.setEmail("test@example.com");
        
        User user2 = new User();
        user2.setId(1L);
        user2.setUsername("testuser");
        user2.setEmail("test@example.com");
        
        User user3 = new User();
        user3.setId(2L);
        user3.setUsername("otheruser");
        user3.setEmail("other@example.com");
        
        // Проверка
        assertThat(user1).isEqualTo(user2);
        assertThat(user1).isNotEqualTo(user3);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
        assertThat(user1.hashCode()).isNotEqualTo(user3.hashCode());
    }
    
    @Test
    public void testToString() {
        // Подготовка
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        
        // Проверка
        String userString = user.toString();
        assertThat(userString).contains("id=1");
        assertThat(userString).contains("username=testuser");
        assertThat(userString).contains("email=test@example.com");
    }
}

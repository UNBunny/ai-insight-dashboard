package com.example.demo.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.example.demo.models.User;

/**
 * Интеграционные тесты для репозитория UserRepository с использованием H2 in-memory базы данных
 */
@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    public void testSaveUser() {
        // Подготовка
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        
        // Сохранение
        User savedUser = userRepository.save(user);
        
        // Проверка
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("password123");
    }
    
    @Test
    public void testFindById() {
        // Подготовка - сохраняем пользователя через EntityManager
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        
        Long userId = entityManager.persistAndGetId(user, Long.class);
        entityManager.flush();
        
        // Выполнение
        Optional<User> foundUser = userRepository.findById(userId);
        
        // Проверка
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    }
    
    @Test
    public void testFindByUsername() {
        // Подготовка - сохраняем пользователя
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        
        entityManager.persist(user);
        entityManager.flush();
        
        // Выполнение
        Optional<User> foundUserOpt = userRepository.findByUsername("testuser");
        
        // Проверка
        assertThat(foundUserOpt).isPresent();
        User foundUser = foundUserOpt.get();
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
    }
    
    @Test
    public void testFindByEmail() {
        // Подготовка - сохраняем пользователя
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        
        entityManager.persist(user);
        entityManager.flush();
        
        // Выполнение
        Optional<User> foundUserOpt = userRepository.findByEmail("test@example.com");
        
        // Проверка
        assertThat(foundUserOpt).isPresent();
        User foundUser = foundUserOpt.get();
        assertThat(foundUser.getUsername()).isEqualTo("testuser");
    }
    
    @Test
    public void testFindAll() {
        // Подготовка - сохраняем нескольких пользователей
        User user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setPassword("password1");
        
        User user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPassword("password2");
        
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();
        
        // Выполнение
        List<User> users = userRepository.findAll();
        
        // Проверка
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getUsername).containsExactlyInAnyOrder("user1", "user2");
    }
    
    @Test
    public void testDeleteById() {
        // Подготовка - сохраняем пользователя
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        
        Long userId = entityManager.persistAndGetId(user, Long.class);
        entityManager.flush();
        
        // Проверяем, что пользователь существует
        assertThat(userRepository.findById(userId)).isPresent();
        
        // Выполнение - удаляем пользователя
        userRepository.deleteById(userId);
        
        // Проверка
        assertThat(userRepository.findById(userId)).isNotPresent();
    }
    
    @Test
    public void testExistsById() {
        // Подготовка - сохраняем пользователя
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        
        Long userId = entityManager.persistAndGetId(user, Long.class);
        entityManager.flush();
        
        // Выполнение и проверка
        assertThat(userRepository.existsById(userId)).isTrue();
        assertThat(userRepository.existsById(999L)).isFalse();
    }
}

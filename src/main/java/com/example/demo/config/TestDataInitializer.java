package com.example.demo.config;

import com.example.demo.models.User;
import com.example.demo.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Класс для инициализации тестовых данных
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.security.test-users.enabled", havingValue = "true", matchIfMissing = false)
public class TestDataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment env;

    /**
     * Создает тестовых пользователей при запуске приложения
     */
    /**
     * Инициализация тестовых пользователей при запуске приложения
     */
    @Bean
    public CommandLineRunner initTestData() {
        return args -> {
            // Создаем или обновляем администратора
            String adminUsername = env.getProperty("app.security.test-users.admin-username");
            String adminPassword = env.getProperty("app.security.test-users.admin-password");
            String adminEmail = env.getProperty("app.security.test-users.admin-email");
            String adminRole = env.getProperty("app.security.test-users.admin-role");
            
            User adminUser = userRepository.findByUsername(adminUsername).orElse(new User());
            adminUser.setUsername(adminUsername);
            adminUser.setEmail(adminEmail);
            adminUser.setPassword(passwordEncoder.encode(adminPassword));
            adminUser.setRole(adminRole);
            adminUser.setEnabled(true);
            userRepository.save(adminUser);
            log.info("Создан/обновлен тестовый администратор: {} с ролью {}", adminUsername, adminRole);

            // Создаем или обновляем обычного пользователя
            String userUsername = env.getProperty("app.security.test-users.user-username");
            String userPassword = env.getProperty("app.security.test-users.user-password");
            String userEmail = env.getProperty("app.security.test-users.user-email");
            String userRole = env.getProperty("app.security.test-users.user-role");
            
            User regularUser = userRepository.findByUsername(userUsername).orElse(new User());
            regularUser.setUsername(userUsername);
            regularUser.setEmail(userEmail);
            regularUser.setPassword(passwordEncoder.encode(userPassword));
            regularUser.setRole(userRole);
            regularUser.setEnabled(true);
            userRepository.save(regularUser);
            log.info("Создан/обновлен тестовый пользователь: {} с ролью {}", userUsername, userRole);
        };
    }
}

package com.example.demo.config;

// Удаляем неиспользуемый импорт
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Конфигурация безопасности приложения
 * 
 * @deprecated This class is deprecated in favor of WebSecurityConfig.
 * It will be removed in future versions. Use WebSecurityConfig instead.
 */
@Configuration
@Deprecated
public class SecurityConfig {
    
    /**
     * Создает и настраивает энкодер паролей с использованием BCrypt
     * 
     * @return настроенный BCryptPasswordEncoder
     * @deprecated Use the passwordEncoder bean from WebSecurityConfig instead.
     */
    // Commenting out the bean to avoid conflicts with WebSecurityConfig
    // @Bean
    public PasswordEncoder legacyPasswordEncoder() {
        // Используем BCrypt с силой 12 для хеширования паролей
        return new BCryptPasswordEncoder(12);
    }
}
